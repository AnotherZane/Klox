package net.fakezane.klox

enum class TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,
    CARET, TILDE,

    // One or two character tokens.
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL, GREATER_GREATER,
    LESS, LESS_EQUAL, LESS_LESS,
    AMPERSAND, AMPERSAND_AMPERSAND,
    PIPE, PIPE_PIPE,

    // Literals.
    IDENTIFIER, STRING, NUMBER,

    // Keywords.
    TRUE, FALSE, NIL,
    IF, ELSE, AND, OR,
    WHILE, FOR,
    VAR, FUN, CLASS, THIS, SUPER, RETURN,
    PRINT,

    EOF
}
