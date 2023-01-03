package net.fakezane.klox.parselets

import net.fakezane.klox.Expr
import net.fakezane.klox.Parser
import net.fakezane.klox.Token
import net.fakezane.klox.TokenType

class CallParselet(precedence: Int) : MixfixParselet(precedence) {
    override fun parse(parser: Parser, left: Expr, token: Token): Expr {
        val arguments: MutableList<Expr> = ArrayList()
        if (!parser.check(TokenType.RIGHT_PAREN)) {
            do {
                if (arguments.size >= 255) {
                    parser.error(parser.peek(), "Cannot have more than 255 arguments.")
                }
                arguments.add(parser.parseExpression())
            } while (parser.match(TokenType.COMMA))
        }
        parser.consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.")
        return Expr.Call(left, token, arguments)
    }
}
