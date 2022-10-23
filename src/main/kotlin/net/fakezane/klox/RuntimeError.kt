package net.fakezane.klox

class RuntimeError(val token: Token, message: String?) : RuntimeException(message)
