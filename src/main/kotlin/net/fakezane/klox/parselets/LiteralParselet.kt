package net.fakezane.klox.parselets

import net.fakezane.klox.Expr
import net.fakezane.klox.Parser
import net.fakezane.klox.Token
import net.fakezane.klox.TokenType.*

class LiteralParselet : PrefixParselet() {
    override fun parse(parser: Parser, token: Token): Expr {
        return when (token.type) {
            TRUE -> Expr.Literal(true)
            FALSE -> Expr.Literal(false)
            NIL -> Expr.Literal(null)
            NUMBER, STRING -> Expr.Literal(token.literal)
            else -> throw parser.error(token, "Failed to parse as literal.")
        }
    }
}
