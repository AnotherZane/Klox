package net.fakezane.klox.parselets

import net.fakezane.klox.Expr
import net.fakezane.klox.Parser
import net.fakezane.klox.Token

class BinaryOperatorParselet(precedence: Int) : MixfixParselet(precedence) {
    override fun parse(parser: Parser, left: Expr, token: Token): Expr {
        val right = parser.parseExpression(getPrecedence())
        return Expr.Binary(left, token, right)
    }
}
