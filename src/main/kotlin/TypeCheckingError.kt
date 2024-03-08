package org.pl

import grammar.StellaRuleContext

class TypeCheckingError private constructor(val errorTag: String, message: String?) :
    Exception("Type Error Tag: [$errorTag]\n$message") {

    private constructor(errorTag: String, ctx: StellaRuleContext, message: String? = "") :
            this(errorTag, "$message at ${ctx.text}") {
    }

    companion object {

        fun missingMain(): TypeCheckingError {
            return TypeCheckingError("ERROR_MISSING_MAIN", "main function is not found in the program")
        }


        /**
         * LIST
         */
        fun unexpectedList(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError(
                "ERROR_UNEXPECTED_LIST", ctx
            )
        }

        fun notAList(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError(
                "ERROR_NOT_A_LIST",
                ctx
            )
        }

        fun ambiguousList(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError(
                "ERROR_AMBIGUOUS_LIST",
                ctx
            )
        }

        /**
         * FUNCTION
         */
        fun notAFunction(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError("ERROR_NOT_A_FUNCTION", ctx)
        }

        fun incorrectNumberOfArgs(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError("ERROR_INCORRECT_NUMBER_OF_ARGUMENTS", ctx)
        }

        fun unexpectedNumberOfParametersInLambda(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError("ERROR_UNEXPECTED_NUMBER_OF_PARAMETERS_IN_LAMBDA", ctx)
        }


        fun undefinedVar(varName: String): TypeCheckingError {
            return TypeCheckingError("ERROR_UNDEFINED_VARIABLE", "undefined variable $varName")
        }

        fun unexpectedLambda(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError("ERROR_UNEXPECTED_LAMBDA", ctx)
        }

        /**
         * TUPLE
         */
        fun unexpectedTuple(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError("ERROR_UNEXPECTED_TUPLE", ctx)
        }

        fun notATuple(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError("ERROR_NOT_A_TUPLE", ctx)
        }

        fun outOfIndexTuple(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError("ERROR_TUPLE_INDEX_OUT_OF_BOUNDS", ctx)
        }

        /**
         * when compare two tuple types
         */
        fun unexpectedTupleLength(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError("ERROR_UNEXPECTED_TUPLE_LENGTH", ctx)
        }

        /**
         * RECORD
         */
        fun unexpectedRecord(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError("ERROR_UNEXPECTED_RECORD", ctx)
        }

        fun recordDuplicatedLabel(ctx: StellaRuleContext, key: String): TypeCheckingError {
            return TypeCheckingError(
                "ERROR_RECORD_DUPLICATE_LABEL",
                "duplicate field $key at ${ctx.text}"
            )
        }

        fun notARecord(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError("ERROR_NOT_A_RECORD", ctx)
        }

        fun unexpectedRecordFieldAccess(ctx: StellaRuleContext, label: String): TypeCheckingError {
            return TypeCheckingError(
                "ERROR_UNEXPECTED_FIELD_ACCESS", ctx, "field $label"
            )
        }

        /**
         * when compare two tuple types
         */
        fun unexpectedRecordField(ctx: StellaRuleContext, label: String): TypeCheckingError {
            return TypeCheckingError(
                "ERROR_UNEXPECTED_RECORD_FIELDS", ctx, "field $label"
            )
        }

        fun missingRecordField(ctx: StellaRuleContext, label: String): TypeCheckingError {
            return TypeCheckingError(
                "ERROR_MISSING_RECORD_FIELDS", ctx, "field $label"
            )
        }


        /**
         *
         */
        fun unexpectedType(
            expectedType: StellaType,
            actualType: StellaType,
            ctx: StellaRuleContext
        ): TypeCheckingError {
            return TypeCheckingError(
                "ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION",
                "expected type $expectedType but got $actualType for expression ${ctx.text}"
            )
        }

        /**
         * SUM
         */
        fun ambiguousSum(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError("ERROR_AMBIGUOUS_SUM_TYPE", ctx)
        }

        fun unexpectedInj(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError("ERROR_UNEXPECTED_INJECTION", ctx)
        }

        /**
         * VARIANT
         */
        fun ambiguousVariant(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError("ERROR_AMBIGUOUS_VARIANT_TYPE", ctx)
        }

        fun unexpectedVariant(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError("ERROR_UNEXPECTED_VARIANT", ctx)
        }

        fun unexpectedVariantLabel(ctx: StellaRuleContext, label: String): TypeCheckingError {
            return TypeCheckingError("ERROR_UNEXPECTED_VARIANT_LABEL", ctx, "label: $label")
        }

        /**
         * #nullary-variant-labels (теги без данных в вариантах)
         */

        /**
         * Вариант (Variant) содержит данные (SomeExprData), хотя ожидается тег без данных (NoTyping);
         */
        fun dataForNullaryVariantLabel(ctx: StellaRuleContext, label: String): TypeCheckingError {
            return TypeCheckingError("ERROR_UNEXPECTED_DATA_FOR_NULLARY_LABEL", ctx, "label $label")
        }
        fun missingDataForVariant(ctx: StellaRuleContext, label: String): TypeCheckingError {
            return TypeCheckingError("ERROR_MISSING_DATA_FOR_LABEL", ctx, "label $label")
        }

    }

}