package org.pl

import grammar.StellaRuleContext
import grammar.gen.StellaParser
import grammar.gen.StellaParser.*


class TypeCheckingVisitor : TypeCheckingCommonVisitor() {
    private val mainName = "main"

    private fun checkExpectation(ctx: StellaRuleContext, type: StellaType) {
        if (ctx.expected != null && ctx.expected != type) {
            throw TypeCheckingError.unexpectedType(ctx.expected, type, ctx)
        }
    }


    private fun inferType(ctx: StellaRuleContext): StellaType {
        ctx.updateLocals()
        ctx.expected = null
        return ctx.accept(this)
    }

    private fun checkType(ctx: StellaRuleContext, expectedType: StellaType?): StellaType {
        ctx.updateLocals()
        ctx.expected = expectedType
        return ctx.accept(this)
    }

    override fun visitProgram(ctx: ProgramContext): StellaType {
        for (decl in ctx.decls) {
            inferType(decl)
        }
        if (ctx.localVariables?.containsKey(mainName) == false) {
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
        return funType
    }

    override fun visitDeclFunGeneric(ctx: DeclFunGenericContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitDeclTypeAlias(ctx: DeclTypeAliasContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitDeclExceptionType(ctx: DeclExceptionTypeContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitDeclExceptionVariant(ctx: DeclExceptionVariantContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitInlineAnnotation(ctx: InlineAnnotationContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitParamDecl(ctx: ParamDeclContext): StellaType {
        val name: String = ctx.name.text
        val type: StellaType = inferType(ctx.paramType)
        ctx.addToParentCtx(name, type)
        return type
    }

    override fun visitFold(ctx: FoldContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitAdd(ctx: AddContext): StellaType {
        TODO("Not yet implemented")
    }


    /**
     * VISIT TYPES
     */
    override fun visitTypeBool(ctx: TypeBoolContext): StellaType {
        return Bool
    }

    override fun visitTypeRef(ctx: TypeRefContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitTypeRec(ctx: TypeRecContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitTypeSum(ctx: TypeSumContext): StellaType {
        return Sum(inferType(ctx.left), inferType(ctx.right))
    }

    override fun visitTypeNat(ctx: TypeNatContext): StellaType {
        return Nat
    }

    override fun visitTypeBottom(ctx: TypeBottomContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitTypeUnit(ctx: TypeUnitContext): StellaType {
        return StellaUnit
    }

    private fun <T : StellaRuleContext> acceptAndGet(params: List<T>): List<StellaType> {
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
        return Fun(acceptAndGet(ctx.paramTypes), inferType(ctx.returnType))
    }

    override fun visitTypeForAll(ctx: TypeForAllContext): StellaType {
        TODO("Not yet implemented")
    }

    /**
     * '{' (types += stellatype (',' types += stellatype)*)? '}' # TypeTuple
     */
    override fun visitTypeTuple(ctx: TypeTupleContext): StellaType {
        return Tuple(acceptAndGet(ctx.types))
    }

    override fun visitTypeTop(ctx: TypeTopContext): StellaType {
        TODO("Not yet implemented")
    }

    /**
     * name = StellaIdent                                        # TypeVar
     */
    override fun visitTypeVar(ctx: TypeVarContext): StellaType {
        TODO("Not yet implemented")
    }

    /**
     *    '<|' (
     *         fieldTypes += variantFieldType (
     *             ',' fieldTypes += variantFieldType
     *         )*
     *     )? '|>'                                                     # TypeVariant
     */
    override fun visitTypeVariant(ctx: TypeVariantContext): StellaType {
        val type = Variant(ctx.fieldTypes
            .associate { it.label.text to it?.type_?.accept(this) })
        checkExpectation(ctx, type)
        return type
    }

    /**
     *  '{'  fieldTypes += recordFieldType (',' fieldTypes += recordFieldType)* '}' # TypeRecord
     */
    override fun visitTypeRecord(ctx: TypeRecordContext): StellaType {
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
        val itemType = inferType(ctx.type_)
        return StellaList(itemType)
    }


    /**
     * '(' type_ = stellatype ')' # TypeParens;
     */
    override fun visitTypeParens(ctx: TypeParensContext): StellaType {
        return inferType(ctx.type_)
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


    override fun visitConstMemory(ctx: ConstMemoryContext): StellaType {
        TODO("Not yet implemented")
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

    override fun visitTryCatch(ctx: TryCatchContext): StellaType {
        TODO("Not yet implemented")
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
            if (funType !is Fun) {
                throw TypeCheckingError.notAFunction(ctx)
            }
            if (funType.args.size != 1 || funType.args[0] != funType.ret) {
                TypeCheckingError.notAFunction(ctx)
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

    /**
     * CONSTANTS
     */
    override fun visitConstTrue(ctx: ConstTrueContext): StellaType {
        checkExpectation(ctx, Bool)
        return Bool
    }

    override fun visitSubtract(ctx: SubtractContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitTypeCast(ctx: TypeCastContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitConstFalse(ctx: ConstFalseContext): StellaType {
        checkExpectation(ctx, Bool)
        return Bool
    }

    override fun visitConstUnit(ctx: ConstUnitContext): StellaType {
        checkExpectation(ctx, StellaUnit)
        return StellaUnit
    }

    override fun visitSequence(ctx: SequenceContext): StellaType {
        TODO("Not yet implemented")
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
        return checkType(ctx.rhs, expectedVarType)
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

    override fun visitTypeAbstraction(ctx: TypeAbstractionContext): StellaType {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
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

    override fun visitMatchCase(ctx: MatchCaseContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitPatternCons(ctx: PatternConsContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitPatternTuple(ctx: PatternTupleContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitPatternList(ctx: PatternListContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitPatternRecord(ctx: PatternRecordContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitPatternVariant(ctx: PatternVariantContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitPatternAsc(ctx: PatternAscContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitPatternInt(ctx: PatternIntContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitPatternInr(ctx: PatternInrContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitPatternTrue(ctx: PatternTrueContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitPatternInl(ctx: PatternInlContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitPatternVar(ctx: PatternVarContext): StellaType {
        ctx.addToParentCtx(ctx.name.text, ctx.expected)
        return ctx.expected
    }


    override fun visitPatternSucc(ctx: PatternSuccContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitPatternFalse(ctx: PatternFalseContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitPatternUnit(ctx: PatternUnitContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitLabelledPattern(ctx: LabelledPatternContext): StellaType {
        TODO("Not yet implemented")
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
            return checkType(ctx.list, StellaList(expectedType))
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

    override fun visitDeref(ctx: DerefContext): StellaType {
        TODO("Not yet implemented")
    }

    /**
     * 'fn' '(' (
     *   paramDecls += paramDecl (',' paramDecls += paramDecl)*
     *   )? ')' '{' 'return' returnExpr = expr '}'       # Abstraction
     */
    override fun visitAbstraction(ctx: AbstractionContext): StellaType {
        val expected = ctx.expected
        if (expected == null) {
            return Fun(acceptAndGet(ctx.paramDecls), inferType(ctx.returnExpr))
        } else {
            if (expected !is Fun) {
                throw TypeCheckingError.unexpectedLambda(ctx)
            }
            if (ctx.paramDecls.size != expected.args.size) {
                throw TypeCheckingError.unexpectedNumberOfParametersInLambda(ctx)
            }

            ctx.paramDecls.zip(expected.args).forEach { (decl, exp) -> checkType(decl, exp) }

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
            if (expectedType.items.size != ctx.exprs.size) {
                throw TypeCheckingError.unexpectedTupleLength(ctx)
            }
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
            ctx.bindings.forEach { checkType(it.rhs, expectedType.items[it.name.text]!!) }

            return expectedType
        }
    }


    override fun visitTypeApplication(ctx: TypeApplicationContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitLetRec(ctx: LetRecContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitTryWith(ctx: TryWithContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitPred(ctx: PredContext): StellaType {
        TODO("Not yet implemented")
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

    override fun visitUnfold(ctx: UnfoldContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitRef(ctx: RefContext): StellaType {
        TODO("Not yet implemented")
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
        return checkType(ctx.expr_, ctx.expected)
    }

    override fun visitTerminatingSemicolon(ctx: StellaParser.TerminatingSemicolonContext): StellaType {
        return checkType(ctx.expr_, ctx.expected)
    }
}