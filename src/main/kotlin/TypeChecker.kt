package org.pl

import grammar.gen.StellaLexer
import grammar.gen.StellaParser
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.misc.ParseCancellationException

import java.io.InputStream
import java.io.OutputStream

class TypeChecker {
    companion object {
        fun check(input: InputStream = System.`in`, output: OutputStream = System.out): Int {
            val code = input.reader().readText()
            val parser = getParser(code)
            val visitor = TypeCheckingVisitor()
            try {
                visitor.visitStart_Program(parser.start_Program())
            } catch (e: TypeCheckingError) {
                System.err.println(e.message)
                return -1
            }
            return 0
        }

        private fun getParser(code: String): StellaParser {
            val charStream = CharStreams.fromString(code)
            val lexer = StellaLexer(charStream)
            lexer.removeErrorListeners()
            lexer.addErrorListener(ThrowingErrorListener.INSTANCE)
            val tokenStream = CommonTokenStream(lexer)

            val parser = StellaParser(tokenStream)
            parser.errorHandler = BailErrorStrategy()
            parser.removeErrorListeners()
            parser.addErrorListener(ThrowingErrorListener.INSTANCE)
            return parser

        }

        private class ThrowingErrorListener : BaseErrorListener() {
            @Throws(ParseCancellationException::class)
            override fun syntaxError(
                recognizer: Recognizer<*, *>?,
                offendingSymbol: Any?,
                line: Int,
                charPositionInLine: Int,
                msg: String,
                e: RecognitionException?
            ) {
                throw ParseCancellationException("line $line:$charPositionInLine $msg")
            }

            companion object {
                val INSTANCE: ThrowingErrorListener = ThrowingErrorListener()
            }
        }
    }
}

