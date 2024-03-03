package org.pl

class TypeCheckingError(errorTag: String, message: String?) : Exception("Type Error Tag: [$errorTag]\n$message") {
    companion object {
        fun unexpectedType(expectedType: StellaType, actualType: StellaType, expr: String): TypeCheckingError {
            return TypeCheckingError(
                "ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION",
                """expected type
  $expectedType
but got
  $actualType
for expression
  $expr
 """
            )
        }
    }
}