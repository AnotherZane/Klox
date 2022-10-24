package net.fakezane.klox.parselets

import net.fakezane.klox.Expr
import net.fakezane.klox.Parser
import net.fakezane.klox.Token

class PostfixOperatorParselet(precedence: Int) : MixfixParselet(precedence) {
    override fun parse(parser: Parser, left: Expr, token: Token): Expr {
        if (left is Expr.Variable) return Expr.Postfix(token, left.name)

        throw parser.error(token, "Invalid left-hand expression for postfix operation.")
    }
}
