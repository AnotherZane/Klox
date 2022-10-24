package net.fakezane.klox.parselets

import net.fakezane.klox.*

abstract class MixfixParselet(private val precedence: Int) {
    abstract fun parse(parser: Parser, left: Expr, token: Token): Expr
    fun getPrecedence(): Int = precedence
}
