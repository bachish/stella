package org.pl

import grammar.StellaRuleContext

class TypeCheckingError private constructor(val errorTag: String, message: String?) :
    Exception("Type Error Tag: [$errorTag]\n$message") {

    private constructor(errorTag: String, ctx: StellaRuleContext, message: String? = "") :
            this(
                errorTag,
                "$message in expression ${ctx.text} at line ${ctx.start.line} col ${ctx.start.charPositionInLine}"
            ) {
    }

    companion object {

        fun missingMain(): TypeCheckingError {
            return TypeCheckingError("ERROR_MISSING_MAIN", "main function is not found in the program")
        }


        fun undefinedVar(varName: String): TypeCheckingError {
            return TypeCheckingError("ERROR_UNDEFINED_VARIABLE", "undefined variable $varName")
        }

        /**
         * LIST
         */
        fun unexpectedList(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError(
                "ERROR_UNEXPECTED_LIST", ctx
            )
        }

        /**
         *  при попытке извлечь голову (Head), извлечь хвост (Tail) или
         * проверить список на наличие элементов (IsEmpty), соответствующий аргумент оказывается
         * не списком (TypeList)
         */
        fun notAList(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError(
                "ERROR_NOT_A_LIST",
                ctx
            )
        }

        fun ambiguousList(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError(
                "ERROR_AMBIGUOUS_LIST_TYPE",
                ctx
            )
        }

        /**
         * FUNCTION
         */

        /**
         * при попытке применить (Application) выражение к аргументу
         * или передать в комбинатор неподвижной точки (Fix), выражение оказывается не функцией;
         * ошибка должна возникать до проверки типа аргумента
         */
        fun notAFunction(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError("ERROR_NOT_A_FUNCTION", ctx)
        }


        /**
         *  в процессе проверки типов анонимная функция (Abstraction)
         * проверяется с не функциональным типом (TypeFun); ошибка должна возникать до проверки
         * типа самой анонимной функции
         */
        fun unexpectedLambda(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError("ERROR_UNEXPECTED_LAMBDA", ctx)
        }

        /**
         *  в процессе проверки параметра анонимной
         * функции (AParamDecl) указанный тип параметра отличается от ожидаемого; ошибка должно
         * возникать до проверки тела анонимной функции
         */
        fun unexpectedTypeForParameter(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError("ERROR_UNEXPECTED_TYPE_FOR_PARAMETER", ctx)
        }


        /**
         * TUPLE
         */
        fun unexpectedTuple(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError("ERROR_UNEXPECTED_TUPLE", ctx)
        }

        /**
         * при попытке извлечь компонент кортежа (DotTuple) из выражения,
         * выражение оказывается не кортежем (TypeTuple)
         */
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

        /***
         * при попытке извлечь поле записи (DotRecord) из выражения,
         * выражение оказывается не записью (TypeRecord)
         */
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
                "ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION", ctx,
                "expected type $expectedType but got $actualType "
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

        /**
         * образец варианта (PatternVariant)
         * содержит тег с данными (SomePatternData), хотя в типе разбираемого выражения
         * этот тег указан без данных (NoTyping);
         */
        fun unexpectedNonNullaryVariantPattern(ctx: StellaRuleContext, label: String): TypeCheckingError {
            return TypeCheckingError("ERROR_UNEXPECTED_NON_NULLARY_VARIANT_PATTERN", ctx, "label $label")
        }

        /**
         * ERROR_UNEXPECTED_NULLARY_VARIANT_PATTERN — образец варианта (PatternVariant)
         * содержит тег без данных (NoPatternData), хотя в типе разбираемого выражения
         * этот тег указан с данными (SomeTyping);
         */
        fun unexpectedNullaryVariantPattern(ctx: StellaRuleContext, label: String): TypeCheckingError {
            return TypeCheckingError("ERROR_UNEXPECTED_NULLARY_VARIANT_PATTERN", ctx, "label $label")
        }



        /**
         * MATCHING
         */
        fun emptyMatching(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError("ERROR_ILLEGAL_EMPTY_MATCHING", ctx)
        }

        /**
         * Образец в match-выражении не соответствует
         * типу разбираемого выражения;
         */
        fun unexpectedPatternForType(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError("ERROR_UNEXPECTED_PATTERN_FOR_TYPE", ctx)
        }

        fun nonExhausPattern(ctx: StellaRuleContext, patternType: StellaType): TypeCheckingError {
            return TypeCheckingError("ERROR_NONEXHAUSTIVE_MATCH_PATTERNS", ctx, "pattern-type: $patternType")
        }

        /**
         * #nullary-functions и #multiparameter-functions:
         *  узлы синтаксического дерева: DeclFun, Abstraction, Application
         */
        fun incorrectArityOfMain(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError("ERROR_INCORRECT_ARITY_OF_MAIN", ctx)
        }

        fun incorrectNumberOfArgs(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError("ERROR_INCORRECT_NUMBER_OF_ARGUMENTS", ctx)
        }

        fun unexpectedNumberOfParametersInLambda(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError("ERROR_UNEXPECTED_NUMBER_OF_PARAMETERS_IN_LAMBDA", ctx)
        }

        /**
         * letrec
         */

        fun ambiguousPatternType(ctx: StellaRuleContext): TypeCheckingError {
            return TypeCheckingError("ERROR_AMBIGUOUS_PATTERN_TYPE", ctx)
        }

    }

}