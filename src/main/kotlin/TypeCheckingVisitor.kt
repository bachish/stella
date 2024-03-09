package org.pl

import grammar.StellaRuleContext
import grammar.gen.StellaParser.*


class TypeCheckingVisitor : TypeCheckingCommonVisitor() {
    private val mainFunName = "main"

    /**
     * After type inference compare inferred type with expected for this term
     */
    private fun checkExpectation(ctx: StellaRuleContext, actualType: StellaType) {
        if (ctx.expected != null && ctx.expected != actualType) {
            throw TypeCheckingError.unexpectedType(ctx.expected, actualType, ctx)
        }
    }

    private inline fun <reified T> checkPatternType(ctx: StellaRuleContext) {
        if (ctx.expected != null && ctx.expected !is T) {
            throw TypeCheckingError.unexpectedPatternForType(ctx)
        }
    }

    private fun inferType(ctx: StellaRuleContext): StellaType {
        return checkType(ctx, null)
    }

    private fun checkPatternType(
        ctx: StellaRuleContext,
        expectedType: StellaType?,
        patternType: StellaType
    ): StellaType {
        ctx.patternExpectedType = patternType
        return checkType(ctx, expectedType)
    }

    private fun checkType(
        ctx: StellaRuleContext,
        expectedType: StellaType?,
        error: TypeCheckingError? = null
    ): StellaType {
        ctx.updateLocals()
        ctx.expected = expectedType
        val actualType = ctx.accept(this)
        if (expectedType != null && ctx.expected != actualType) {
            if (error != null) {
                throw error
            }
            assert(ctx.expected == actualType)
        }
        return actualType
    }

    override fun visitProgram(ctx: ProgramContext): StellaType {
        for (decl in ctx.decls) {
            inferType(decl)
        }
        if (ctx.localVariables?.containsKey(mainFunName) == false) {
            throw TypeCheckingError.missingMain()
        }
        return StellaUnit
    }


    override fun visitDeclFun(ctx: DeclFunContext): StellaType {
        val funName = ctx.name.text
        val returnType = inferType(ctx.returnType)
        val paramTypes = ctx.paramDecls.map { inferType(it) }
        val funType = Fun(paramTypes, returnType)

        ctx.addToParentCtx(funName, funType)

        ctx.localDecls.forEach { inferType(it) }

        checkType(ctx.returnExpr, returnType)

        if (funName == mainFunName && funType.args.size != 1) {
            throw TypeCheckingError.incorrectArityOfMain(ctx)
        }
        return funType
    }

    override fun visitParamDecl(ctx: ParamDeclContext): StellaType {
        val name: String = ctx.name.text
        val type: StellaType = inferType(ctx.paramType)
        ctx.addToParentCtx(name, type)
        return type
    }


    /**
     * VISIT TYPES
     */
    override fun visitTypeBool(ctx: TypeBoolContext): StellaType {
        checkPatternType<Bool>(ctx)
        return Bool
    }


    override fun visitTypeSum(ctx: TypeSumContext): StellaType {
        checkPatternType<Sum>(ctx)
        return Sum(inferType(ctx.left), inferType(ctx.right))
    }

    override fun visitTypeNat(ctx: TypeNatContext): StellaType {
        checkPatternType<Nat>(ctx)
        return Nat
    }

    override fun visitTypeUnit(ctx: TypeUnitContext): StellaType {
        checkPatternType<StellaUnit>(ctx)
        return StellaUnit
    }

    private fun <T : StellaRuleContext> inferTypes(params: List<T>): List<StellaType> {
        val paramTypes = params.map {
            inferType(it)
        }
        return paramTypes
    }

    /**
     * 'fn' '(' (
     *      paramTypes += stellatype (',' paramTypes += stellatype)*
     *      )? ')' '->' returnType = stellatype                        # TypeFun
     */
    override fun visitTypeFun(ctx: TypeFunContext): StellaType {
        checkPatternType<Fun>(ctx)
        return Fun(inferTypes(ctx.paramTypes), inferType(ctx.returnType))
    }

    /**
     * '{' (types += stellatype (',' types += stellatype)*)? '}' # TypeTuple
     */
    override fun visitTypeTuple(ctx: TypeTupleContext): StellaType {
        checkPatternType<Tuple>(ctx)
        return Tuple(inferTypes(ctx.types))
    }

    /**
     *    '<|' (
     *         fieldTypes += variantFieldType (
     *             ',' fieldTypes += variantFieldType
     *         )*
     *     )? '|>'                                                     # TypeVariant
     */
    override fun visitTypeVariant(ctx: TypeVariantContext): StellaType {
        checkPatternType<Variant>(ctx)
        val types = ctx.fieldTypes
            .associate {
                it.updateLocals()
                it.label.text to if (it.type_ != null) inferType(it.type_) else null
            }
        val labelOrder = ctx.fieldTypes.map { it.label.text }.toList()
        val type = Variant(types, labelOrder)
        checkExpectation(ctx, type)
        return type
    }

    /**
     *  '{'  fieldTypes += recordFieldType (',' fieldTypes += recordFieldType)* '}' # TypeRecord
     */
    override fun visitTypeRecord(ctx: TypeRecordContext): StellaType {
        checkPatternType<Record>(ctx)
        val labelsCount = ctx.fieldTypes.groupingBy { it.label.text }.eachCount()
        val duplicate = labelsCount.entries.firstOrNull { it.value > 1 }
        if (duplicate != null) {
            throw TypeCheckingError.recordDuplicatedLabel(ctx, duplicate.key)
        }
        val labels: List<String> = ctx.fieldTypes.map { it.label.text }
        val items = ctx.fieldTypes.associate { it.label.text to inferType(it.type_) }
        return Record(items, labels)
    }


    /**
     * '[' type_ = stellatype ']' # TypeList
     */
    override fun visitTypeList(ctx: TypeListContext): StellaType {
        checkPatternType<StellaList>(ctx)
        val itemType = inferType(ctx.type_)
        return StellaList(itemType)
    }


    /**
     * '(' type_ = stellatype ')' # TypeParens;
     */
    override fun visitTypeParens(ctx: TypeParensContext): StellaType {
        return checkType(ctx.type_, ctx.expected)
    }


    /**
     * VISIT EXPR
     */

    /**
     * PROJECTIONS
     */

    /**
     * expr_ = expr '.' label = StellaIdent # DotRecord
     */
    override fun visitDotRecord(ctx: DotRecordContext): StellaType {
        val recordType = inferType(ctx.expr_)

        if (recordType !is Record) {
            throw TypeCheckingError.notARecord(ctx)
        }

        val label = ctx.label.text

        val srcType = recordType.items[label]
            ?: throw TypeCheckingError.unexpectedRecordFieldAccess(ctx, label)

        checkExpectation(ctx, srcType)
        return srcType
    }

    /**
     * '[' (exprs += expr (',' exprs += expr)*)? ']' # List
     */
    override fun visitList(ctx: ListContext): StellaType {
        val expectedType = ctx.expected
        if (expectedType == null) {
            if (ctx.exprs.size == 0) {
                throw TypeCheckingError.ambiguousList(ctx)
            }
            val items = ctx.exprs.iterator()
            val itemType = inferType(items.next())
            while (items.hasNext()) {
                checkType(items.next(), itemType)
            }
            return StellaList(itemType)
        } else {
            if (expectedType !is StellaList) {
                throw TypeCheckingError.unexpectedList(ctx)
            }
            for (item in ctx.exprs) {
                checkType(item, expectedType.elemType)
            }
            return expectedType
        }
    }

    /**
     * expr_ = expr '.' index = INTEGER   # DotTuple
     */
    override fun visitDotTuple(ctx: DotTupleContext): StellaType {
        val tupleType = inferType(ctx.expr_)

        if (tupleType !is Tuple) {
            throw TypeCheckingError.notATuple(ctx)
        }

        val id = ctx.index.text.toInt()

        if (id < 1 || id > tupleType.items.size) {
            throw TypeCheckingError.outOfIndexTuple(ctx)
        }
        val srcType = tupleType.items[id - 1]

        checkExpectation(ctx, srcType)
        return srcType
    }

    /**
     *  (T -> T) -> T
     *  | 'fix' '(' expr_ = expr ')'                                     # Fix
     *
     */
    override fun visitFix(ctx: FixContext): StellaType {
        val expectedType = ctx.expected
        if (ctx.expected == null) {
            val funType = inferType(ctx.expr_)
            if (funType !is Fun || funType.args.size != 1) {
                throw TypeCheckingError.notAFunction(ctx)
            }
            val argType = funType.args[0]
            if (argType != funType.ret) {
                throw TypeCheckingError.unexpectedType(Fun(listOf(argType), argType), funType, ctx)
            }
            return funType.ret

        } else {
            checkType(ctx.expr_, Fun(listOf(expectedType), expectedType))
            return expectedType
        }
    }

    /**
     * 'let' patternBindings+=patternBinding (',' patternBindings+=patternBinding)* 'in' body = expr           # Let
     */
    override fun visitLet(ctx: LetContext): StellaType {
        ctx.patternBindings.forEach { inferType(it) }
        return checkType(ctx.body, ctx.expected)
    }

    override fun visitParenthesisedPattern(ctx: ParenthesisedPatternContext): StellaType {
        val type = checkType(ctx.pattern_, ctx.expected)
        ctx.addAllToParentCtx()
        return type
    }


    /**
     * CONSTANTS
     */
    override fun visitConstTrue(ctx: ConstTrueContext): StellaType {
        checkExpectation(ctx, Bool)
        return Bool
    }

    override fun visitConstFalse(ctx: ConstFalseContext): StellaType {
        checkExpectation(ctx, Bool)
        return Bool
    }

    override fun visitConstUnit(ctx: ConstUnitContext): StellaType {
        checkExpectation(ctx, StellaUnit)
        return StellaUnit
    }

    override fun visitConstInt(ctx: ConstIntContext): StellaType {
        checkExpectation(ctx, Nat)
        return Nat
    }

    /**
     * '<|' label = StellaIdent ('=' rhs = expr)? '|>' # Variant
     * for example, "ret <Ok = 12>"
     */
    override fun visitVariant(ctx: VariantContext): StellaType {
        val expectedType = ctx.expected ?: throw TypeCheckingError.ambiguousVariant(ctx)
        if (expectedType !is Variant) {
            throw TypeCheckingError.unexpectedVariant(ctx)
        }
        val varLabel = ctx.label.text
        if (varLabel !in expectedType.labelToType) {
            throw TypeCheckingError.unexpectedVariantLabel(ctx, varLabel)
        }
        val expectedVarType = expectedType.labelToType[varLabel]
        if (ctx.rhs == null && expectedVarType == null) {
            return expectedType
        }
        if (expectedVarType == null) {
            throw TypeCheckingError.dataForNullaryVariantLabel(ctx, varLabel)
        }
        if (ctx.rhs == null) {
            throw TypeCheckingError.missingDataForVariant(ctx, varLabel)
        }
        checkType(ctx.rhs, expectedVarType)
        return expectedType
    }


    /**
     *  name = StellaIdent                 # Var
     */
    override fun visitVar(ctx: VarContext): StellaType {
        val varName = ctx.name.text
        if (!ctx.localVariables.containsKey(varName)) {
            throw TypeCheckingError.undefinedVar(varName)
        }
        val varType = ctx.localVariables[varName]!!
        checkExpectation(ctx, varType)
        return varType
    }

    override fun visitInl(ctx: InlContext): StellaType {
        val expectedType = ctx.expected ?: throw TypeCheckingError.ambiguousSum(ctx)
        if (expectedType !is Sum) {
            throw TypeCheckingError.unexpectedInj(ctx)
        }
        checkType(ctx.expr_, expectedType.left)
        return expectedType
    }


    override fun visitInr(ctx: InrContext): StellaType {
        val expectedType = ctx.expected ?: throw TypeCheckingError.ambiguousSum(ctx)
        if (expectedType !is Sum) {
            throw TypeCheckingError.unexpectedInj(ctx)
        }
        checkType(ctx.expr_, expectedType.right)
        return expectedType
    }

    /**
     * 'match' expr_ = expr '{' (
     *         cases += matchCase ('|' cases += matchCase)*
     *     )? '}'                                         # Match
     */
    override fun visitMatch(ctx: MatchContext): StellaType {
        if (ctx.cases.size == 0) {
            throw TypeCheckingError.emptyMatching(ctx)
        }
        val patternType = inferType(ctx.expr_)
        val expectedBodyType = checkPatternType(ctx.cases[0], ctx.expected, patternType)
        for (case in ctx.cases.asSequence().drop(1)) {
            checkPatternType(case, expectedBodyType, patternType)
        }
        ExhaustiveChecker(ctx).check(ctx.cases.map { it.pattern_ }, patternType)
        return expectedBodyType
    }


    /**
     * 'cons' '(' head = expr ',' tail = expr ')'                     # ConsList
     */
    override fun visitConsList(ctx: ConsListContext): StellaType {
        val expectedType = ctx.expected
        if (expectedType == null) {
            val headType = inferType(ctx.head)
            return checkType(ctx.tail, StellaList(headType))
        } else {
            if (expectedType !is StellaList) {
                throw TypeCheckingError.unexpectedList(ctx)
            }
            checkType(ctx.head, expectedType.elemType)
            checkType(ctx.tail, expectedType)
            return expectedType
        }
    }

    /**
     * patternBinding: pat=pattern '=' rhs=expr ;
     */
    override fun visitPatternBinding(ctx: PatternBindingContext): StellaType {
        val exprType = inferType(ctx.rhs)
        checkType(ctx.pat, exprType)
        ctx.addAllToParentCtx()
        return exprType
    }

    override fun visitBinding(ctx: BindingContext): StellaType {
        TODO("Not yet implemented")
    }

    /**
     * matchCase: pattern_ = pattern '=>' expr_ = expr;
     */
    override fun visitMatchCase(ctx: MatchCaseContext): StellaType {
        checkType(ctx.pattern_, ctx.patternExpectedType)
        return checkType(ctx.expr_, ctx.expected)
    }


    /**
     * 'cons' '(' head = pattern ',' tail = pattern ')'          # PatternCons
     */
    override fun visitPatternCons(ctx: PatternConsContext): StellaType {
        val error = TypeCheckingError.unexpectedPatternForType(ctx)
        val pattType = ctx.expected
        if (pattType !is StellaList) {
            throw error
        }
        checkType(ctx.head, pattType.elemType)
        checkType(ctx.tail, pattType)
        ctx.addAllToParentCtx()
        return pattType
    }

    /**
     *  '{' (patterns += pattern (',' patterns += pattern)*)? '}' # PatternTuple
     */
    override fun visitPatternTuple(ctx: PatternTupleContext): StellaType {
        val error = TypeCheckingError.unexpectedPatternForType(ctx)
        val pattType = ctx.expected
        if (pattType !is Tuple) {
            throw error
        }
        pattType.checkLength(ctx.patterns.size, error)
        for ((expect, patt) in pattType.items.zip(ctx.patterns)) {
            checkType(patt, expect)
        }
        ctx.addAllToParentCtx()
        return ctx.expected
    }

    /**
     * '[' (patterns += pattern (',' patterns += pattern)*)? ']' # PatternList
     */
    override fun visitPatternList(ctx: PatternListContext): StellaType {
        val error = TypeCheckingError.unexpectedPatternForType(ctx)
        val pattType = ctx.expected
        if (pattType !is StellaList) {
            throw error
        }
        for (listElem in ctx.patterns) {
            checkType(listElem, pattType.elemType)
        }
        ctx.addAllToParentCtx()
        return pattType
    }

    /**
     *'{' (  patterns += labelledPattern (
     *             ',' patterns += labelledPattern
     *         )*
     *     )? '}'                                                      # PatternRecord
     *
     *     Filed order can not important (important only in record-type)
     */
    override fun visitPatternRecord(ctx: PatternRecordContext): StellaType {
        val error = TypeCheckingError.unexpectedPatternForType(ctx)
        val pattType = ctx.expected
        if (pattType !is Record) {
            throw error
        }

        if (ctx.patterns.map { it.label.text }.toSet() != pattType.items.keys) {
            throw error
        }

        for (labelledPatt in ctx.patterns) {
            checkType(labelledPatt, pattType.items[labelledPatt.label.text])
        }
        ctx.addAllToParentCtx()
        return ctx.expected
    }

    /**
     *    '<|' label = StellaIdent ('=' pattern_ = pattern)? '|>'     # PatternVariant
     */
    override fun visitPatternVariant(ctx: PatternVariantContext): StellaType {
        val label: String = ctx.label.text
        val pattType = ctx.expected
        if (pattType !is Variant) {
            throw TypeCheckingError.unexpectedPatternForType(ctx)
        }
        val type = pattType.getType(label, TypeCheckingError.unexpectedPatternForType(ctx))
        if (type == null && ctx.pattern_ == null) {
            return ctx.expected
        }
        if (type == null) {
            throw TypeCheckingError.unexpectedNonNullaryVariantPattern(ctx, label)
        }
        if (ctx.pattern_ == null) {
            throw TypeCheckingError.unexpectedNullaryVariantPattern(ctx, label)
        }
        checkType(ctx.pattern_, type)
        ctx.addAllToParentCtx()
        return ctx.expected
    }

    /**
     * pattern_ = pattern 'as' type_ = stellatype                # PatternAsc
     */
    override fun visitPatternAsc(ctx: PatternAscContext): StellaType {
        val type = checkType(ctx.type_, ctx.expected)
        checkType(ctx.pattern_, type)
        ctx.addAllToParentCtx()
        return type
    }

    /**
     * n = INTEGER                                               # PatternInt
     */
    override fun visitPatternInt(ctx: PatternIntContext): StellaType {
        val expectedType = ctx.expected
        if (expectedType !is Nat) {
            throw TypeCheckingError.unexpectedPatternForType(ctx)
        }
        ctx.addAllToParentCtx()
        return Nat
    }

    override fun visitPatternInr(ctx: PatternInrContext): StellaType {
        val expectedType = ctx.expected
        if (expectedType !is Sum) {
            throw TypeCheckingError.unexpectedPatternForType(ctx)
        }
        checkType(ctx.pattern_, expectedType.right)
        ctx.addAllToParentCtx()
        return ctx.expected
    }

    override fun visitPatternTrue(ctx: PatternTrueContext): StellaType {
        if (ctx.expected != null && ctx.expected !is Bool) {
            throw TypeCheckingError.unexpectedPatternForType(ctx)
        }
        return Bool
    }

    override fun visitPatternInl(ctx: PatternInlContext): StellaType {
        val expectedType = ctx.expected
        if (expectedType !is Sum) {
            throw TypeCheckingError.unexpectedPatternForType(ctx)
        }
        checkType(ctx.pattern_, expectedType.left)
        ctx.addAllToParentCtx()
        return ctx.expected
    }

    override fun visitPatternVar(ctx: PatternVarContext): StellaType {
        if (ctx.expected == null) {
            throw TypeCheckingError.ambiguousPatternType(ctx)
        }
        ctx.addToParentCtx(ctx.name.text, ctx.expected)
        ctx.addAllToParentCtx()
        return ctx.expected
    }

    override fun visitPatternSucc(ctx: PatternSuccContext): StellaType {
        if (ctx.expected != null && ctx.expected !is Nat) {
            throw TypeCheckingError.unexpectedPatternForType(ctx)
        }
        checkType(ctx.pattern_, Nat)
        ctx.addAllToParentCtx()
        return Nat
    }

    override fun visitPatternFalse(ctx: PatternFalseContext): StellaType {
        if (ctx.expected != null && ctx.expected !is Bool) {
            throw TypeCheckingError.unexpectedPatternForType(ctx)
        }
        return Bool
    }

    override fun visitPatternUnit(ctx: PatternUnitContext): StellaType {
        if (ctx.expected != null && ctx.expected !is StellaUnit) {
            throw TypeCheckingError.unexpectedPatternForType(ctx)
        }
        return StellaUnit
    }

    /**
     * labelledPattern: label = StellaIdent '=' pattern_ = pattern;
     *
     */
    override fun visitLabelledPattern(ctx: LabelledPatternContext): StellaType {
        val type = checkType(ctx.pattern_, ctx.expected)
        ctx.addAllToParentCtx()
        return type
    }

    /**
     * 'List::head' '(' list = expr ')'                               # Head
     */
    override fun visitHead(ctx: HeadContext): StellaType {
        val expectedType = ctx.expected
        if (expectedType == null) {
            val listType = inferType(ctx.list)
            if (listType !is StellaList) {
                throw TypeCheckingError.notAList(ctx)
            }
            return listType.elemType
        } else {
            checkType(ctx.list, StellaList(expectedType))
            return expectedType
        }
    }


    /**
     * 'List::isempty' '(' list = expr ')'                            # IsEmpty
     */
    override fun visitIsEmpty(ctx: IsEmptyContext): StellaType {
        val listType = inferType(ctx.list)
        if (listType !is StellaList) {
            throw TypeCheckingError.notAList(ctx)
        }
        checkExpectation(ctx, Bool)
        return Bool
    }


    /**
     * 'List::tail' '(' list = expr ')'                               # Tail
     */
    override fun visitTail(ctx: TailContext): StellaType {
        if (ctx.expected != null && ctx.expected is StellaList) {
            return checkType(ctx.list, ctx.expected)
        }
        val listType = inferType(ctx.list)
        if (listType !is StellaList) {
            throw TypeCheckingError.notAList(ctx)
        }
        checkExpectation(ctx, listType)
        return listType
    }

    override fun visitSucc(ctx: SuccContext): StellaType {
        checkType(ctx.n, Nat)
        checkExpectation(ctx, Nat)
        return Nat
    }

    /**
     * 'Nat::iszero' '(' n = expr ')'                                 # IsZero
     */
    override fun visitIsZero(ctx: IsZeroContext): StellaType {
        checkType(ctx.n, Nat)
        checkExpectation(ctx, Bool)
        return Bool
    }

    /**
     * fun = expr '(' (args += expr (',' args += expr)*)? ')'
     */
    override fun visitApplication(ctx: ApplicationContext): StellaType {
        val funType = inferType(ctx.`fun`)
        if (funType !is Fun) {
            throw TypeCheckingError.notAFunction(ctx)
        }
        if (ctx.args.size != funType.args.size) {
            throw TypeCheckingError.incorrectNumberOfArgs(ctx)
        }

        funType.args.zip(ctx.args).stream().forEach { (expType, actCtx) ->
            checkType(actCtx, expType)
        }

        checkExpectation(ctx, funType.ret)
        return funType.ret
    }

    /**
     * 'fn' '(' (
     *   paramDecls += paramDecl (',' paramDecls += paramDecl)*
     *   )? ')' '{' 'return' returnExpr = expr '}'       # Abstraction
     */
    override fun visitAbstraction(ctx: AbstractionContext): StellaType {
        val expected = ctx.expected
        if (expected == null) {
            return Fun(inferTypes(ctx.paramDecls), inferType(ctx.returnExpr))
        } else {
            if (expected !is Fun) {
                throw TypeCheckingError.unexpectedLambda(ctx)
            }
            if (ctx.paramDecls.size != expected.args.size) {
                throw TypeCheckingError.unexpectedNumberOfParametersInLambda(ctx)
            }

            ctx.paramDecls.zip(expected.args).forEach { (decl, exp) ->
                checkType(decl, exp, TypeCheckingError.unexpectedTypeForParameter(ctx))
            }

            checkType(ctx.returnExpr, expected.ret)
            return expected
        }
    }


    /**
     * '{' (exprs += expr (',' exprs += expr)*)? '}' # Tuple
     */
    override fun visitTuple(ctx: TupleContext): StellaType {
        val expectedType = ctx.expected
        if (expectedType == null) {
            return Tuple(ctx.exprs.map { inferType(it) })
        } else {
            if (expectedType !is Tuple) {
                throw TypeCheckingError.unexpectedTuple(ctx)
            }
            expectedType.checkLength(ctx.exprs.size, TypeCheckingError.unexpectedTupleLength(ctx))

            for ((exp, decl) in expectedType.items.zip(ctx.exprs)) {
                checkType(decl, exp)
            }
            return expectedType
        }

    }

    /**
     * '{' bindings += binding (',' bindings += binding)* '}' # Record
     */
    override fun visitRecord(ctx: RecordContext): StellaType {
        val expectedType = ctx.expected

        val labelsCount = ctx.bindings.groupingBy { it.name.text }.eachCount()
        val duplicate = labelsCount.entries.firstOrNull { it.value > 1 }
        if (duplicate != null) {
            throw TypeCheckingError.recordDuplicatedLabel(ctx, duplicate.key)
        }
        val items: Map<String, StellaType>
        val labels: List<String> = ctx.bindings.map { it.name.text }
        if (expectedType == null) {
            items = ctx.bindings.associate { it.name.text to inferType(it.rhs) }
            return Record(items, labels)
        } else {
            if (expectedType !is Record) {
                throw TypeCheckingError.unexpectedRecord(ctx)
            }
            val unexpectedLabels = labelsCount.keys.subtract(expectedType.items.keys)
            if (unexpectedLabels.isNotEmpty()) {
                throw TypeCheckingError.unexpectedRecordField(ctx, unexpectedLabels.first())
            }
            val missingLabels = expectedType.items.keys.subtract(labelsCount.keys)
            if (missingLabels.isNotEmpty()) {
                throw TypeCheckingError.missingRecordField(ctx, missingLabels.first())
            }
            ctx.bindings.forEach {
                it.updateLocals()
                checkType(it.rhs, expectedType.items[it.name.text]!!)
            }

            return expectedType
        }
    }


    /**
     * 'letrec' patternBindings+=patternBinding
     *              (',' patternBindings+=patternBinding)* 'in' body = expr
     * Pattern must be annotated with type!
     * Instead of simple rec, which arguments types can be inferred from rhs
     */
    override fun visitLetRec(ctx: LetRecContext): StellaType {
        ctx.patternBindings.forEach { inferType(it) }
        return checkType(ctx.body, ctx.expected)
    }

    /**
     * letRecPatternBinding: pat=pattern '=' rhs=expr ;
     */
    override fun visitLetRecPatternBinding(ctx: LetRecPatternBindingContext): StellaType {
        val pattType = inferType(ctx.pattern())
        checkType(ctx.rhs, pattType)
        ctx.addAllToParentCtx()
        return pattType
    }


    /**
     *     | expr_ = expr 'as' type_ = stellatype # TypeAsc
     */
    override fun visitTypeAsc(ctx: TypeAscContext): StellaType {
        val type = inferType(ctx.type_)
        checkType(ctx.expr_, type)
        checkExpectation(ctx, type)
        return type
    }

    /**
     *   | 'Nat::rec' '(' n = expr ',' initial = expr ',' step = expr ')' # NatRec
     */
    override fun visitNatRec(ctx: NatRecContext): StellaType {
        checkType(ctx.n, Nat)
        val retType = checkType(ctx.initial, ctx.expected)
        checkType(ctx.step, Fun(listOf(Nat), Fun(listOf(retType), retType)))
        return retType
    }

    /**
     * 'if' condition = expr 'then' thenExpr = expr 'else' elseExpr = expr # If
     */
    override fun visitIf(ctx: IfContext): StellaType {
        checkType(ctx.condition, Bool)
        val resType = checkType(ctx.thenExpr, ctx.expected)
        checkType(ctx.elseExpr, resType)
        return resType
    }

    /**
     * '(' expr_ = expr ')'    # ParenthesisedExpr
     */
    override fun visitParenthesisedExpr(ctx: ParenthesisedExprContext): StellaType {
        val type = checkType(ctx.expr_, ctx.expected)
        ctx.addAllToParentCtx()
        return type
    }

    override fun visitTerminatingSemicolon(ctx: TerminatingSemicolonContext): StellaType {
        return checkType(ctx.expr_, ctx.expected)
    }
}