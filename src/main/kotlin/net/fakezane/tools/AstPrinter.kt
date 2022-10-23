package net.fakezane.tools

import net.fakezane.klox.Expr

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

    fun formatExpr(expr: Expr): String = when (expr) {
        is Expr.Binary -> parenthesize(expr.operator.lexeme, expr.left, expr.right)
        is Expr.Grouping -> parenthesize("group", expr.expression)
        is Expr.Unary -> parenthesize(expr.operator.lexeme, expr.right)
        is Expr.Literal -> expr.value?.toString() ?: "nil"
        else -> "Unknown"
    }

    fun print(expr: Expr?) {
        if (expr == null)
            println("Empty Expression")
        else
            println(formatExpr(expr))
    }
}
