package net.fakezane.lox

class RuntimeError(val token: Token, message: String?) : RuntimeException(message)
