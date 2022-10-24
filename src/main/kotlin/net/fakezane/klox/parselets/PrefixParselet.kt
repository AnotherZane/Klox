package net.fakezane.klox.parselets

import net.fakezane.klox.*

abstract class PrefixParselet {
    abstract fun parse(parser: Parser, token: Token): Expr
}
