package net.fakezane.klox.parselets

import net.fakezane.klox.Expr
import net.fakezane.klox.Parser
import net.fakezane.klox.Token

class LogicalOperatorParselet(precedence: Int) : MixfixParselet(precedence) {
    override fun parse(parser: Parser, left: Expr, token: Token): Expr {
        val right = parser.parseExpression(getPrecedence())
        return Expr.Logical(left, token, right)
    }
}
