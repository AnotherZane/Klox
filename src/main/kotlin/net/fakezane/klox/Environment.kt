package net.fakezane.klox

class Environment(val enclosing: Environment? = null) {
    private val values = HashMap<String, Any?>()

    fun get(name: Token): Any? {
        if (values.containsKey(name.lexeme)) return values[name.lexeme]

        if (enclosing != null) return enclosing.get(name)

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun assign(name: Token, value: Any?): Any? {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
            return value
        }

        if (enclosing != null) {
            return enclosing.assign(name, value)
        }

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun define(name: String, value: Any?): Any? {
        values[name] = value
        return value
    }
}
