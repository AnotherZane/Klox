package net.fakezane.klox

import Klox
import net.fakezane.klox.TokenType.*

class Interpreter {
    private var environment = Environment()

    fun interpret(statements: List<Stmt?>, isREPL: Boolean = false) {
        try {
            var returnedValue: Any? = null
            for (statement in statements) {
                returnedValue = execute(statement!!)
            }

            if (isREPL && returnedValue != null && returnedValue != Unit) println(stringify(returnedValue))

        } catch (error: RuntimeError) {
            Klox.runtimeError(error)
        }
    }

    fun evaluate(expr: Expr): Any? {
        return when (expr) {
            is Expr.Assign -> {
                val value = evaluate(expr.value)
                environment.assign(expr.name, value)
                value
            }
            is Expr.Literal -> expr.value
            is Expr.Logical -> {
                val left = evaluate(expr.left)

                if (expr.operator.type == OR || expr.operator.type == PIPE_PIPE) {
                    if (isTruthy(left)) return left
                } else {
                    if (!isTruthy(left)) return left
                }

                evaluate(expr.right)
            }
            is Expr.Grouping -> evaluate(expr.expression)
            is Expr.Prefix -> evaluatePrefix(expr)
            is Expr.Postfix -> evaluatePostfix(expr)
            is Expr.Binary -> evaluateBinary(expr)
            is Expr.Variable -> environment.get(expr.name)
            else -> null // Unreachable
        }
    }

    fun execute(stmt: Stmt): Any? {
        return when (stmt) {
            is Stmt.Block -> executeBlock(stmt.statements, Environment(environment))
            is Stmt.Expression -> evaluate(stmt.expression)
            is Stmt.If -> {
                var value: Any? = null

                if (isTruthy(evaluate(stmt.condition)))
                    value = execute(stmt.thenBranch)
                else if (stmt.elseBranch != null)
                    value = execute(stmt.elseBranch)

                value
            }
            is Stmt.Print -> {
                val value = evaluate(stmt.expression)
                println(stringify(value))
                value
            }
            is Stmt.Var -> {
                var value: Any? = null

                if (stmt.initializer != null)
                    value = evaluate(stmt.initializer)

                environment.define(stmt.name.lexeme, value)
            }
            is Stmt.While -> {
                while (isTruthy(evaluate(stmt.condition))) {
                    execute(stmt.body)
                }
            }
            else -> null // Unreachable
        }
    }

    fun executeBlock(statements: List<Stmt?>, environment: Environment): Any? {
        val previous = this.environment
        var returnedValue: Any? = null
        try {
            this.environment = environment
            for (statement in statements) {
                returnedValue = execute(statement!!)
            }
        }
        finally {
            this.environment = previous
        }
        return returnedValue
    }

    private fun evaluatePrefix(expr: Expr.Prefix): Any? {
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
            MINUS_MINUS -> {
                checkNumberOperand(expr.operator, right)
                environment.assign((expr.right as Expr.Variable).name, (right as Double) - 1)
                right
            }
            PLUS_PLUS -> {
                checkNumberOperand(expr.operator, right)
                environment.assign((expr.right as Expr.Variable).name, (right as Double) + 1)
                right
            }
            else -> null // Unreachable
        }
    }

    private fun evaluatePostfix(expr: Expr.Postfix): Any? {
        val left = environment.get(expr.left)

        return when (expr.operator.type) {
            MINUS_MINUS -> {
                checkNumberOperand(expr.operator, left)
                environment.assign(expr.left, (left as Double) - 1)
            }
            PLUS_PLUS -> {
                checkNumberOperand(expr.operator, left)
                environment.assign(expr.left, (left as Double) + 1)
            }
            else -> null // Unreachable
        }
    }

    private fun evaluateBinary(expr: Expr.Binary): Any? {
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
                } else if (left is String && right is String) {
                    left + right
                } else throw RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            }

            AMPERSAND -> {
                checkNumberOperands(expr.operator, left, right)
                asInt(left).and(asInt(right))
            }
            PIPE -> {
                checkNumberOperands(expr.operator, left, right)
                asInt(left).or(asInt(right))
            }
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
