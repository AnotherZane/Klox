package net.fakezane.tool

import java.io.PrintWriter
import kotlin.system.exitProcess


object GenerateAst {
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size != 1) {
            System.err.println("Usage: generate_ast <output directory>")
            exitProcess(64)
        }
        val outputDir = args[0]
        defineAst(outputDir, "Expr", mapOf(
            "Binary" to "left: Expr, operator: Token, right: Expr",
            "Grouping" to "expression: Expr",
            "Literal" to "value: Any?",
            "Unary" to "operator: Token, right: Expr"
            )
        )
    }

    private fun defineAst(outputDir: String, baseName: String, types: Map<String, String>) {
        val path = "$outputDir/$baseName.kt"
        val writer = PrintWriter(path, "UTF-8")

        writer.println("package net.fakezane.lox;")
        writer.println()
        writer.println("abstract class $baseName {")

        for (type in types) {
            defineType(writer, baseName, type.key, type.value)
        }

        writer.println("}")
        writer.close()
    }

    private fun defineType(writer: PrintWriter, baseName: String, className: String, fieldList: String) {
        val fields = fieldList.split(", ").map { it -> "val $it" }.joinToString(", ")
        writer.print("    class $className(")
        writer.print(fields)
        writer.println(") : $baseName()")
    }

}
