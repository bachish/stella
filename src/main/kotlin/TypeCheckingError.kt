package org.pl

class TypeCheckingError(val errorTag: String, message: String?) : Exception("Type Error Tag: [$errorTag]\n$message") {
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
        fun emptyContext( ): TypeCheckingError{
            return TypeCheckingError("EMPTY_CONTEXT", "can't parse type")
        }
        fun unexpectedType(errorTag: String,
                           expectedType: StellaType,
                           actualExpr: String,
                           expectedTypePrefix: String = "",
                           actualTypePrefix: String = "") : TypeCheckingError{
            return  TypeCheckingError(errorTag,
                "expected an expression of a $expectedTypePrefix " +
                "type \n$expectedType but got $actualTypePrefix $actualExpr")
        }
    }

}