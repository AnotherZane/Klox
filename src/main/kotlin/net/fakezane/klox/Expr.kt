package net.fakezane.klox;

abstract class Expr {
    class Assign(val name: Token, val value: Expr) : Expr()
    class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()
    class Grouping(val expression: Expr) : Expr()
    class Literal(val value: Any?) : Expr()
    class Logical(val left: Expr, val operator: Token, val right: Expr) : Expr()
    class Prefix(val operator: Token, val right: Expr) : Expr()
    class Postfix(val operator: Token, val left: Token) : Expr()
    class Variable(val name: Token) : Expr()
}
