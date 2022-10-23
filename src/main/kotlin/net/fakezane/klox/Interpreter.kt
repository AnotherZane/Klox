package net.fakezane.klox

import Klox
import net.fakezane.klox.TokenType.*

class Interpreter {

    fun interpret(expression: Expr) {
        try {
            val value = evaluate(expression)
            println(stringify(value))
        } catch (error: RuntimeError) {
            Klox.runtimeError(error)
        }
    }

    fun evaluate(expr: Expr): Any? {
        return when (expr) {
            is Expr.Literal -> expr.value
            is Expr.Grouping -> evaluate(expr.expression)
            is Expr.Unary -> {
                val right = evaluate(expr.right)

                return when (expr.operator.type) {
                    MINUS -> {
                        checkNumberOperand(expr.operator, right)
                        -(right as Double)
                    }
                    BANG -> !isTruthy(right)
                    TILDE -> {
                        checkNumberOperand(expr.operator, right)
                        asInt(right).inv()
                    }
                    else -> null // Unreachable
                }
            }
            is Expr.Binary -> {
                val left = evaluate(expr.left)
                val right = evaluate(expr.right)

                return when (expr.operator.type) {
                    GREATER -> {
                        checkNumberOperands(expr.operator, left, right)
                        left as Double > right as Double
                    }
                    GREATER_EQUAL -> {
                        checkNumberOperands(expr.operator, left, right)
                        left as Double >= right as Double
                    }
                    LESS -> {
                        checkNumberOperands(expr.operator, left, right)
                        (left as Double) < right as Double
                    }
                    LESS_EQUAL -> {
                        checkNumberOperands(expr.operator, left, right)
                        left as Double <= right as Double
                    }

                    BANG_EQUAL -> !isEqual(left, right);
                    EQUAL_EQUAL -> isEqual(left, right);

                    MINUS -> {
                        checkNumberOperands(expr.operator, left, right)
                        left as Double - right as Double
                    }
                    SLASH -> {
                        checkNumberOperands(expr.operator, left, right)
                        left as Double / right as Double
                    }
                    STAR -> {
                        checkNumberOperands(expr.operator, left, right)
                        left as Double * right as Double
                    }
                    PLUS -> {
                        if (left is Double && right is Double) {
                            left + right
                        }
                        else if (left is String && right is String) {
                            left + right
                        }
                        else throw RuntimeError(expr.operator,"Operands must be two numbers or two strings.");
                    }

                    AMPERSAND -> {
                        checkNumberOperands(expr.operator, left, right)
                        asInt(left).and(asInt(right))
                    }
                    AMPERSAND_AMPERSAND -> isTruthy(left) and isTruthy(right)
                    PIPE -> {
                        checkNumberOperands(expr.operator, left, right)
                        asInt(left).or(asInt(right))
                    }
                    PIPE_PIPE -> isTruthy(left) or isTruthy(right)
                    CARET -> {
                        checkNumberOperands(expr.operator, left, right)
                        asInt(left).xor(asInt(right))
                    }
                    GREATER_GREATER -> {
                        checkNumberOperands(expr.operator, left, right)
                        asInt(left).shr(asInt(right))
                    }
                    LESS_LESS -> {
                        checkNumberOperands(expr.operator, left, right)
                        asInt(left).shl(asInt(right))
                    }
                    else -> null // Unreachable
                }
            }
            else -> null // Unreachable
        }
    }

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand is Double) return
        throw RuntimeError(operator, "Operand must be a number.")
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if (left is Double && right is Double) return
        throw RuntimeError(operator, "Operands must be numbers.")
    }

    private fun isTruthy(obj: Any?): Boolean {
        if (obj == null) return false
        return if (obj is Boolean) obj else true
    }

    private fun isEqual(a: Any?, b: Any?): Boolean {
        if (a == null && b == null) return true
        return if (a == null) false else a == b
    }

    private fun asInt(a: Any?) = (a as Double).toInt()

    private fun stringify(obj: Any?): String {
        if (obj == null) return "nil"
        if (obj is Double) {
            var text = obj.toString()
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2)
            }
            return text
        }
        return obj.toString()
    }

}
