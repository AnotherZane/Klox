package net.fakezane.klox.parselets

import net.fakezane.klox.Expr
import net.fakezane.klox.Parser
import net.fakezane.klox.Token
import net.fakezane.klox.TokenType

class GroupingParselet : PrefixParselet() {
    override fun parse(parser: Parser, token: Token): Expr {
        val expr = parser.parseExpression()
        parser.consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
        return Expr.Grouping(expr)
    }
}
