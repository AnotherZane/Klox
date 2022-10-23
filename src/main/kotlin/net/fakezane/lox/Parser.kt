package net.fakezane.lox

import Klox
import net.fakezane.lox.TokenType.*


class Parser(val tokens: List<Token>) {
    private class ParseError : RuntimeException()

    private var current = 0;

    fun parse(): Expr? {
        return try {
            expression()
        } catch (error: ParseError) {
            null
        }
    }

    private fun expression(): Expr = equality()

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
