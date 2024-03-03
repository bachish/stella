package org.pl

fun main() {
    val code = """
        language core;
        fn main(n : Nat) -> Bool {
          return 0
        }
    """.trimIndent()
    val stream = code.byteInputStream()
    TypeChecker.check(stream)
}