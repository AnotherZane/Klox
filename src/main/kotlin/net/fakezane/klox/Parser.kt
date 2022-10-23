package net.fakezane.klox

import Klox
import net.fakezane.klox.Expr.Assign
import net.fakezane.klox.TokenType.*


class Parser(val tokens: List<Token>, private val isREPL: Boolean = false) {
    private class ParseError : RuntimeException()

    private var current = 0;

    fun parse(): List<Stmt?> {
        val statements: MutableList<Stmt?> = ArrayList()
        while (!isAtEnd()) {
            statements.add(declaration())
        }

        return statements
    }

    private fun declaration(): Stmt? {
        return try {
            if (match(VAR)) varDeclaration() else statement()
        } catch (error: ParseError) {
            synchronize()
            null
        }
    }

    private fun varDeclaration(): Stmt {
        val name = consume(IDENTIFIER, "Expect variable name.")

        var initializer: Expr? = null
        if (match(EQUAL)) {
            initializer = expression()
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.")
        return Stmt.Var(name, initializer)
    }

    private fun statement(): Stmt {
        return if (match(PRINT))
            printStatement()
        else if (match(LEFT_BRACE))
            Stmt.Block(block())
        else expressionStatement()
    }

    private fun printStatement(): Stmt {
        val value = expression()
        consume(SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(SEMICOLON, "Expect ';' after expression.")
        return Stmt.Expression(expr)
    }

    private fun block(): List<Stmt?> {
        val statements: MutableList<Stmt?> = ArrayList()

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration())
        }

        consume(RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    private fun expression(): Expr = assignment()

    private fun assignment(): Expr {
        val expr = equality()

        if (match(EQUAL)) {
            val equals = previous()
            val value = assignment()

            if (expr is Expr.Variable) return Assign(expr.name, value)

            error(equals, "Invalid assignment target.")
        }

        return expr
    }


    private fun equality(): Expr = leftAssociative(::bitwise, BANG_EQUAL, EQUAL_EQUAL)

    private fun bitwise(): Expr = leftAssociative(::comparison, AMPERSAND, PIPE, CARET, GREATER_GREATER, LESS_LESS)

    private fun comparison(): Expr = leftAssociative(::term, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL, AMPERSAND_AMPERSAND, PIPE_PIPE)

    private fun term(): Expr = leftAssociative(::factor, MINUS, PLUS)

    private fun factor(): Expr = leftAssociative(::unary, SLASH, STAR)

    private fun unary(): Expr {
        if (match(BANG, MINUS, TILDE)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }

        return primary()
    }

    private fun primary(): Expr {
        if (match(FALSE)) return Expr.Literal(false)
        if (match(TRUE)) return Expr.Literal(true)
        if (match(NIL)) return Expr.Literal(null)
        if (match(NUMBER, STRING)) return Expr.Literal(previous().literal)
        if (match(IDENTIFIER)) return Expr.Variable(previous())
        if (match(LEFT_PAREN)) {
            val expr = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Grouping(expr)
        }
        throw error(peek(), "Expected expression.")
    }

    private fun leftAssociative(function: () -> Expr, vararg types: TokenType): Expr {
        var expr = function()

        while (match(*types)){
            val operator: Token = previous()
            val right = function()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }


    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        if (isREPL && isAtEnd()) return Token(SEMICOLON, ";", null, 1)
        throw error(peek(), message)
    }

    private fun check(type: TokenType): Boolean {
        return if (isAtEnd()) false else peek().type === type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean {
        return peek().type === EOF
    }

    private fun peek(): Token {
        return tokens.get(current)
    }

    private fun previous(): Token {
        return tokens.get(current - 1)
    }

    private fun error(token: Token, message: String): ParseError {
        Klox.error(token, message)
        return ParseError()
    }

    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().type === SEMICOLON) return
            when (peek().type) {
                CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> return
                else -> advance()
            }
        }
    }
}
