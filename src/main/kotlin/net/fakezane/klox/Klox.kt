import net.fakezane.klox.*
import net.fakezane.tools.AstPrinter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess


object Klox {
    private val interpreter = Interpreter()
    var hadError = false
    var hadRuntimeError = false
    var isREPL = false

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size > 1) {
            println("Usage: klox [script]")
            exitProcess(64)
        }
        else if (args.size == 1) {
            runFile(args[0])
        }
        else {
            isREPL = true
            runPrompt()
        }
    }

    private fun runFile(path: String) {
        val bytes: ByteArray = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()))

        if (hadError) exitProcess(65)
        if (hadRuntimeError) exitProcess(70);
    }

    private fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)

        while (true) {
            print("> ")
            val line = reader.readLine() ?: break
            run(line)
            hadError = false
        }
    }

    private fun run(source: String) {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()
        val parser = Parser(tokens, isREPL)
        val statements = parser.parse()

        // Stop if there was a syntax error.
        if (hadError) return

        interpreter.interpret(statements, isREPL)
    }

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    fun error(token: Token, message: String) {
        if (token.type === TokenType.EOF)
            report(token.line, " at end", message)
        else
            report(token.line, " at '${token.lexeme}'", message)
    }

    fun runtimeError(error: RuntimeError) {
        System.err.println("${error.message}\n[line ${error.token.line}]")
        hadRuntimeError = true
    }

    private fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] Error$where: $message")
        hadError = true
    }
}
