package net.fakezane.klox.parselets

import net.fakezane.klox.Expr
import net.fakezane.klox.Parser
import net.fakezane.klox.Token

class AssignmentParselet(precedence: Int) : MixfixParselet(precedence) {
    override fun parse(parser: Parser, left: Expr, token: Token): Expr {
        val value = parser.parseExpression(getPrecedence())
        if (left is Expr.Variable) return Expr.Assign(left.name, value)

        throw parser.error(token, "Invalid assignment target.")
    }
}
