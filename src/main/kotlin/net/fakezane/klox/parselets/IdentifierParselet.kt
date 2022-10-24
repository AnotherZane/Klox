package net.fakezane.klox.parselets

import net.fakezane.klox.Expr
import net.fakezane.klox.Parser
import net.fakezane.klox.Token

class IdentifierParselet : PrefixParselet() {
    override fun parse(parser: Parser, token: Token): Expr {
        return Expr.Variable(token)
    }
}
