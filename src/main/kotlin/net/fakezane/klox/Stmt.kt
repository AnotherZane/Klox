package net.fakezane.klox;

abstract class Stmt {
    class Expression(val expression: Expr) : Stmt()
    class Print(val expression: Expr) : Stmt()
}
