package net.fakezane.klox

import Klox
import net.fakezane.klox.TokenType.*
import net.fakezane.klox.parselets.*


class Parser(val tokens: List<Token>, private val isREPL: Boolean = false) {
    class ParseError : RuntimeException()

    private var current = 0;
    private val prefixParselets = HashMap<TokenType, PrefixParselet>()
    private val mixfixParselets = HashMap<TokenType, MixfixParselet>()

    init {
        registerPrefixes(LiteralParselet(), TRUE, FALSE, NIL, NUMBER, STRING)
        registerPrefixes(IdentifierParselet(), IDENTIFIER)
        registerPrefixes(GroupingParselet(), LEFT_PAREN)
        registerPrefixes(PrefixOperatorParselet(), BANG, MINUS, TILDE, MINUS_MINUS, PLUS_PLUS)
        registerMixfix(PostfixOperatorParselet(Precedence.POSTFIX), MINUS_MINUS, PLUS_PLUS)
        registerMixfix(BinaryOperatorParselet(Precedence.FACTOR), SLASH, STAR)
        registerMixfix(BinaryOperatorParselet(Precedence.TERM), MINUS, PLUS)
        registerMixfix(BinaryOperatorParselet(Precedence.COMPARISON), GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)
        registerMixfix(BinaryOperatorParselet(Precedence.BITWISE), AMPERSAND, PIPE, CARET, GREATER_GREATER, LESS_LESS)
        registerMixfix(BinaryOperatorParselet(Precedence.EQUALITY), BANG_EQUAL, EQUAL_EQUAL)
        registerMixfix(LogicalOperatorParselet(Precedence.AND), AND, AMPERSAND_AMPERSAND)
        registerMixfix(LogicalOperatorParselet(Precedence.OR), OR, PIPE_PIPE)
        registerMixfix(AssignmentParselet(Precedence.ASSIGNMENT), EQUAL)
        registerMixfix(CallParselet(Precedence.FUNCTION), LEFT_PAREN)
    }

    fun register(type: TokenType, parselet: PrefixParselet) {
        prefixParselets[type] = parselet
    }

    fun register(type: TokenType, parselet: MixfixParselet) {
        mixfixParselets[type] = parselet
    }

    fun registerPrefixes(parselet: PrefixParselet, vararg types: TokenType) {
        for (type in types) register(type, parselet)
    }

    fun registerMixfix(parselet: MixfixParselet, vararg types: TokenType) {
        for (type in types) register(type, parselet)
    }

    fun parse(): List<Stmt?> {
        val statements: MutableList<Stmt?> = ArrayList()
        while (!isAtEnd()) {
            statements.add(declaration())
        }

        return statements
    }

    private fun declaration(): Stmt? {
        return try {
            if (match(FUN)) function("function")
            else if (match(VAR)) varDeclaration() else statement()
        } catch (error: ParseError) {
            synchronize()
            null
        }
    }

    private fun varDeclaration(): Stmt {
        val name = consume(IDENTIFIER, "Expect variable name.")

        var initializer: Expr? = null
        if (match(EQUAL)) {
            initializer = parseExpression()
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.")
        return Stmt.Var(name, initializer)
    }

    private fun statement(): Stmt {
        return when (matcha(FOR, IF, PRINT, WHILE, LEFT_BRACE, RETURN)?.type) {
            FOR -> forStatement()
            IF -> ifStatement()
            PRINT -> printStatement()
            WHILE -> whileStatement()
            LEFT_BRACE -> Stmt.Block(block())
            RETURN -> returnStatement()
            else -> expressionStatement()
        }
    }

    private fun forStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'for'.")

        val initializer: Stmt? = if (match(SEMICOLON)) {
            null
        } else if (match(VAR)) {
            varDeclaration()
        } else {
            expressionStatement()
        }

        var condition: Expr? = null
        if (!check(SEMICOLON)) {
            condition = parseExpression()
        }
        consume(SEMICOLON, "Expect ';' after loop condition.")

        var increment: Expr? = null
        if (!check(RIGHT_PAREN)) {
            increment = parseExpression()
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.")

        var body = statement()

        if (increment != null) {
            body = Stmt.Block(listOf(body, Stmt.Expression(increment)))
        }

        if (condition == null) condition = Expr.Literal(true)
        body = Stmt.While(condition, body)

        if (initializer != null) {
            body = Stmt.Block(listOf(initializer, body))
        }

        return body
    }

    private fun ifStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = parseExpression()
        consume(RIGHT_PAREN, "Expect ')' after if condition.")

        val thenBranch = statement()
        var elseBranch: Stmt? = null

        if (match(ELSE)) elseBranch = statement()

        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun printStatement(): Stmt {
        val value = parseExpression()
        consume(SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    private fun returnStatement(): Stmt {
        val keyword = previous()
        var value: Expr? = null
        if (!check(SEMICOLON)) {
            value = parseExpression()
        }

        consume(SEMICOLON, "Expect ';' after return value.")
        return Stmt.Return(keyword, value)
    }

    private fun whileStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = parseExpression()
        consume(RIGHT_PAREN, "Expect ')' after condition.")

        val body = statement()
        return Stmt.While(condition, body)
    }

    private fun block(): List<Stmt?> {
        val statements: MutableList<Stmt?> = ArrayList()

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration())
        }

        consume(RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    private fun expressionStatement(): Stmt {
        val expr = parseExpression()
        consume(SEMICOLON, "Expect ';' after expression.")
        return Stmt.Expression(expr)
    }

    private fun function(kind: String): Stmt.Function? {
        val name = consume(IDENTIFIER, "Expect $kind name.")
        consume(LEFT_PAREN, "Expect '(' after $kind name.")
        val parameters: MutableList<Token> = ArrayList()
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size >= 255) {
                    error(peek(), "Cannot have more than 255 parameters.")
                }
                parameters.add(consume(IDENTIFIER, "Expect parameter name."))
            } while (match(COMMA))
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.")

        consume(LEFT_BRACE, "Expect '{' before $kind body.")
        val body = block()
        return Stmt.Function(name, parameters, body)
    }

    fun parseExpression(): Expr = parseExpression(0)

    fun parseExpression(precedence: Int): Expr {
        var token = advance()
        val prefix = prefixParselets[token.type] ?: throw error(token, "Expected expression.")

        var left = prefix.parse(this, token)

        while (precedence < getPrecedence()) {
            token = advance()
            val mixfix = mixfixParselets[token.type]

            if (mixfix != null) {
                left = mixfix.parse(this, left, token)
            }
        }
        return left
    }

    private fun getPrecedence(): Int {
        val parser= mixfixParselets[peek().type]
        return parser?.getPrecedence() ?: 0
    }

    fun matcha(vararg types: TokenType): Token? {
        for (type in types) {
            if (check(type)) {
                return advance()
            }
        }
        return null
    }

    fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        if (isREPL && isAtEnd()) return Token(SEMICOLON, ";", null, 1)
        throw error(peek(), message)
    }

    fun check(type: TokenType): Boolean {
        return if (isAtEnd()) false else peek().type === type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean {
        return peek().type === EOF
    }

    fun peek(): Token {
        return tokens.get(current)
    }

    private fun previous(): Token {
        return tokens.get(current - 1)
    }

    fun error(token: Token, message: String): ParseError {
        Klox.error(token, message)
        return ParseError()
    }

    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().type === SEMICOLON) return
            when (peek().type) {
                CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> return
                else -> advance()
            }
        }
    }
}
