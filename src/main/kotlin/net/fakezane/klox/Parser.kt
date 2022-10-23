package net.fakezane.klox

import Klox
import net.fakezane.klox.TokenType.*
import java.util.*


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
        return if (match(FOR))
            forStatement()
        else if (match(IF))
            ifStatement()
        else if (match(PRINT))
            printStatement()
        else if (match(WHILE))
            whileStatement()
        else if (match(LEFT_BRACE))
            Stmt.Block(block())
        else expressionStatement()
    }

    private fun forStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'for'.")

        val initializer: Stmt? = if (match(SEMICOLON)) {
            null
        } else if (match(VAR)) {
            varDeclaration()
        } else {
            expressionStatement()
        }

        var condition: Expr? = null
        if (!check(SEMICOLON)) {
            condition = expression()
        }
        consume(SEMICOLON, "Expect ';' after loop condition.")

        var increment: Expr? = null
        if (!check(RIGHT_PAREN)) {
            increment = expression()
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.")

        var body = statement()

        if (increment != null) {
            body = Stmt.Block(listOf(body, Stmt.Expression(increment)))
        }

        if (condition == null) condition = Expr.Literal(true)
        body = Stmt.While(condition, body)

        if (initializer != null) {
            body = Stmt.Block(listOf(initializer, body))
        }

        return body
    }

    private fun ifStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after if condition.")

        val thenBranch = statement()
        var elseBranch: Stmt? = null

        if (match(ELSE)) elseBranch = statement()

        return Stmt.If(condition, thenBranch, elseBranch)
    }


    private fun printStatement(): Stmt {
        val value = expression()
        consume(SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    private fun whileStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after condition.")

        val body = statement()
        return Stmt.While(condition, body)
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
        val expr = or()

        if (match(EQUAL)) {
            val equals = previous()
            val value = assignment()

            if (expr is Expr.Variable) return Expr.Assign(expr.name, value)

            error(equals, "Invalid assignment target.")
        }

        return expr
    }

    private fun or(): Expr {
        var expr = and()

        while (match(OR, PIPE_PIPE)) {
            val operator = previous()
            val right = and()
            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }

    private fun and(): Expr {
        var expr = equality()

        while (match(AND, AMPERSAND_AMPERSAND)) {
            val operator = previous()
            val right = equality()
            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }

    private fun equality(): Expr = leftAssociative(::bitwise, BANG_EQUAL, EQUAL_EQUAL)

    private fun bitwise(): Expr = leftAssociative(::comparison, AMPERSAND, PIPE, CARET, GREATER_GREATER, LESS_LESS)

    private fun comparison(): Expr = leftAssociative(::term, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)

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
            val operator = previous()
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
