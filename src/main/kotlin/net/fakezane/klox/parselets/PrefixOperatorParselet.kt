package net.fakezane.klox.parselets

import net.fakezane.klox.Expr
import net.fakezane.klox.Parser
import net.fakezane.klox.Token
import net.fakezane.klox.TokenType.*

class PrefixOperatorParselet : PrefixParselet() {
    override fun parse(parser: Parser, token: Token): Expr {
        val operand = parser.parseExpression()

        if (!(operand is Expr.Variable) && (token.type == MINUS_MINUS || token.type == PLUS_PLUS))
            throw parser.error(token, "Invalid right-hand expression for prefix operation.")

        return Expr.Prefix(token, operand)
    }
}
