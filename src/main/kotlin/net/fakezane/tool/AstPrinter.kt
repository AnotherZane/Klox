package net.fakezane.tool

import net.fakezane.lox.Expr
import net.fakezane.lox.Token
import net.fakezane.lox.TokenType

object AstPrinter {
    private fun parenthesize(name: String, vararg exprs: Expr): String {
        val builder = StringBuilder()
        builder.append("(").append(name)

        for (expr in exprs) {
            builder.append(" ")
            builder.append(formatExpr(expr))
        }

        builder.append(")")
        return builder.toString()
    }

    private fun formatExpr(expr: Expr): String = when (expr) {
        is Expr.Binary -> parenthesize(expr.operator.lexeme, expr.left, expr.right)
        is Expr.Grouping -> parenthesize("group", expr.expression)
        is Expr.Unary -> parenthesize(expr.operator.lexeme, expr.right)
        is Expr.Literal -> expr.value?.toString() ?: "nil"
        else -> "Unknown"
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val expression: Expr = Expr.Binary(
            Expr.Unary(
                Token(TokenType.MINUS, "-", null, 1),
                Expr.Literal(123)
            ),
            Token(TokenType.STAR, "*", null, 1),
            Expr.Grouping(
                Expr.Literal(45.67)
            )
        )
        println(formatExpr(expression))
    }

}
