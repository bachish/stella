package org.pl

import grammar.StellaRuleContext
import grammar.gen.StellaParser
import grammar.gen.StellaParser.*
import java.util.stream.Collectors


class TypeCheckingVisitor : TypeCheckingCommonVisitor() {
    private val mainName = "main"

    private fun checkBeforeInference(expectedType: StellaType, actualCtx: StellaRuleContext) {
        when (actualCtx) {
            is AbstractionContext -> if (expectedType !is Fun) {
                throw TypeCheckingError.unexpectedType(
                    "ERROR_UNEXPECTED_LAMBDA",
                    expectedType,
                    actualCtx.text,
                    " non-function ",
                    " an anonymous function "
                )
            }

            is TupleContext -> if (expectedType !is Tuple) {
                throw TypeCheckingError.unexpectedType(
                    "ERROR_UNEXPECTED_TUPLE",
                    expectedType,
                    actualCtx.text,
                    " non-tuple ",
                    " a tuple "
                )
            }

            is RecordContext -> if (expectedType !is Record) {
                throw TypeCheckingError.unexpectedType(
                    "ERROR_UNEXPECTED_RECORD",
                    expectedType,
                    actualCtx.text,
                    " non-record ",
                    " a record "
                )
            }

            is ListContext -> if (expectedType !is Record) {
                throw TypeCheckingError.unexpectedType(
                    "ERROR_UNEXPECTED_LIST",
                    expectedType,
                    actualCtx.text,
                    " non-list ",
                    " a list "
                )
            }
//            is InlContext -> if (expectedType !is Inl) {
//                throw TypeCheckingError.unexpectedType(
//                    "ERROR_UNEXPECTED_INJECTION",
//                    expectedType,
//                    actualCtx.text,
//                    " non-injection ",
//                    " an left injection "
//                )
//            }
//            is InrContext -> if (expectedType !is Record) {
//                throw TypeCheckingError.unexpectedType(
//                    "ERROR_UNEXPECTED_INJECTION",
//                    expectedType,
//                    actualCtx.text,
//                    " non-injection ",
//                    " an right injection "
//                )
//            }
        }

    }


    private fun checkType(expectedType: StellaType, actualCtx: StellaRuleContext) {
        checkBeforeInference(expectedType, actualCtx)

        val actualType = actualCtx.accept(this)
        return checkType(expectedType, actualType, actualCtx.text)
    }

    private fun checkRecordType(expectedType: Record, actualType: Record, code: String) {
        if (expectedType.items == actualType.items) {
            return
        }
        for ((label, type) in expectedType.items) {
            val type_ = actualType.items[label]
            if (type_ == null || type_ != type) {
                throw TypeCheckingError("ERROR_MISSING_RECORD_FIELDS", "at $code")
            }
        }
        for ((label, type) in actualType.items) {
            val type_ = expectedType.items[label]
            if (type_ == null || type_ != type) {
                throw TypeCheckingError("ERROR_UNEXPECTED_RECORD_FIELDS", "at $code")
            }
        }
    }

    private fun checkType(expectedType: StellaType, actualType: StellaType, exprView: String) {
        if (expectedType is Record && actualType is Record) {
            checkRecordType(expectedType, actualType, exprView)
        }


        if (expectedType != actualType) {
            throw TypeCheckingError.unexpectedType(expectedType, actualType, exprView)
        }
    }


    override fun visitProgram(ctx: ProgramContext): StellaType {
        visitChildren(ctx)
        if (ctx.localVariables?.containsKey(mainName) == false) {
            throw TypeCheckingError("ERROR_MISSING_MAIN", "main function is not found in the program")
        }
        return StellaUnit
    }


    override fun visitDeclFun(ctx: DeclFunContext): StellaType {

        ctx.updateLocals()

        val funName = ctx.name.text
        val expectedType = ctx.returnType.accept(this)

        val paramTypes = ctx.paramDecls.stream().map { visitParamDecl(it) }.collect(Collectors.toList())
        val funType = Fun(paramTypes, expectedType)
        (ctx.parent as StellaRuleContext).localVariables[funName] = funType

        ctx.localDecls.forEach { it.accept(this) }

        checkType(expectedType, ctx.returnExpr)
        return expectedType
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
        val type: StellaType = ctx.paramType.accept(this)
        val par = ctx.parent as (StellaRuleContext)
        par.localVariables[name] = type
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
        TODO("Not yet implemented")
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
        val paramTypes = params.stream().map {
            it.updateLocals()
            it.accept(this)
        }.collect(Collectors.toList())
        return paramTypes
    }

    /**
     * 'fn' '(' (
     *      paramTypes += stellatype (',' paramTypes += stellatype)*
     *      )? ')' '->' returnType = stellatype                        # TypeFun
     */
    override fun visitTypeFun(ctx: TypeFunContext): StellaType {
           ctx.updateLocals()

        return Fun(acceptAndGet(ctx.paramTypes), ctx.returnType.accept(this))
    }

    override fun visitTypeForAll(ctx: TypeForAllContext): StellaType {
        TODO("Not yet implemented")
    }
//    | 'forall' (types += StellaIdent)* '.' type_ = stellatype          # TypeForAll
//    | 'µ' var = StellaIdent '.' type_ = stellatype             # TypeRec
//    | left = stellatype '+' right = stellatype                 # TypeSum

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
        ctx.updateLocals()

        val varName = ctx.name.text
        if (!ctx.localVariables.containsKey(varName)) {
            throw TypeCheckingError("ERROR_UNDEFINED_VARIABLE", "undefined variable $varName")
        }
        return ctx.localVariables[varName]!!
    }

    override fun visitTypeVariant(ctx: TypeVariantContext): StellaType {
        TODO("Not yet implemented")
    }

    /**
     *  '{'  fieldTypes += recordFieldType (',' fieldTypes += recordFieldType)* '}' # TypeRecord
     */
    override fun visitTypeRecord(ctx: TypeRecordContext): StellaType {
        ctx.updateLocals()
        val items = hashMapOf<String, StellaType>()
        ctx.fieldTypes.stream()
            .forEach {
                it.updateLocals()
                val name: String = it.label.text
                val type: StellaType = it.type_.accept(this)
                if (items.containsKey(name)) {
                    throw TypeCheckingError("ERROR_RECORD_DUPLICATE_LABEL", "at ${ctx.text}")
                } else items[name] = type
            }
        return Record(items)
    }

    /**
     * '[' type_ = stellatype ']' # TypeList
     */
    override fun visitTypeList(ctx: TypeListContext): StellaType {
        ctx.updateLocals()

        val itemType = ctx.type_.accept(this)
        return StellaList(itemType)
    }

    override fun visitRecordFieldType(ctx: RecordFieldTypeContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitVariantFieldType(ctx: VariantFieldTypeContext): StellaType {
        TODO("Not yet implemented")
    }

    /**
     * '(' type_ = stellatype ')' # TypeParens;
     */
    override fun visitTypeParens(ctx: TypeParensContext): StellaType {
        ctx.updateLocals()
        return ctx.type_.accept(this)
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
        ctx.updateLocals()

        val recordType = ctx.expr_.accept(this)

        if (recordType !is Record) {
            throw TypeCheckingError("ERROR_NOT_A_RECORD", "")
        }

        val label = ctx.label.text

        val srcType = if (recordType.items.containsKey(label)) {
            recordType.items[label]!!
        } else {
            throw TypeCheckingError(
                "ERROR_UNEXPECTED_FIELD_ACCESS",
                "at ${ctx.text}"
            )
        }

        return srcType
    }

    override fun visitGreaterThan(ctx: GreaterThanContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitEqual(ctx: EqualContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitThrow(ctx: ThrowContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitMultiply(ctx: MultiplyContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitConstMemory(ctx: ConstMemoryContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitList(ctx: ListContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitTryCatch(ctx: TryCatchContext): StellaType {
        TODO("Not yet implemented")
    }

    /**
     * expr_ = expr '.' index = INTEGER   # DotTuple
     */
    override fun visitDotTuple(ctx: DotTupleContext): StellaType {
        ctx.updateLocals()

        val tupleType = ctx.expr_.accept(this)

        if (tupleType !is Tuple) {
            throw TypeCheckingError("ERROR_NOT_A_TUPLE", "")
        }

        val id = ctx.index.text.toInt()

        val srcType = if (id < tupleType.items.size) {
            tupleType.items[id]
        } else {
            throw TypeCheckingError("ERROR!!", "")
        }

        return srcType
    }

    override fun visitFix(ctx: FixContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitLet(ctx: LetContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitAssign(ctx: AssignContext): StellaType {
        TODO("Not yet implemented")
    }


    /**
     * CONSTANTS
     */
    override fun visitConstTrue(ctx: ConstTrueContext): StellaType {
        return Bool
    }

    override fun visitSubtract(ctx: SubtractContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitTypeCast(ctx: TypeCastContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitConstFalse(ctx: ConstFalseContext): StellaType {
        return Bool
    }

    override fun visitConstUnit(ctx: ConstUnitContext): StellaType {
        return StellaUnit
    }

    override fun visitSequence(ctx: SequenceContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitConstInt(ctx: ConstIntContext): StellaType {
        return Nat
    }

    override fun visitVariant(ctx: VariantContext): StellaType {
        TODO("Not yet implemented")
    }

    //    | mem = MemoryAddress                # ConstMemory
    /**
     *  name = StellaIdent                 # Var
     */
    override fun visitVar(ctx: VarContext): StellaType {
        ctx.updateLocals()

        val varName = ctx.name.text
        if (!ctx.localVariables.containsKey(varName)) {
            throw TypeCheckingError("ERROR_UNDEFINED_VARIABLE", "undefined variable $varName")
        }
        return ctx.localVariables[varName]!!
    }

    override fun visitTypeAbstraction(ctx: TypeAbstractionContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitDivide(ctx: DivideContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitLessThan(ctx: LessThanContext): StellaType {
        TODO("Not yet implemented")
    }


//    | 'panic!'                    # Panic
//    | 'throw' '(' expr_=expr ')'  # Throw
//    | 'try' '{' tryExpr=expr '}' 'catch' '{' pat=pattern '=>' fallbackExpr=expr '}'  # TryCatch
//    | 'try' '{' tryExpr=expr '}' 'with' '{' fallbackExpr=expr '}'  # TryWith

    //    | 'inl' '(' expr_=expr ')'                     # Inl
//    | 'inr' '(' expr_=expr ')'                     # Inr
    override fun visitInl(ctx: InlContext): StellaType {
        return super.visitInl(ctx)
    }


    override fun visitInr(ctx: InrContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitMatch(ctx: MatchContext): StellaType {
        TODO("Not yet implemented")
    }


    /**
     * 'cons' '(' head = expr ',' tail = expr ')'                     # ConsList
     */
    override fun visitConsList(ctx: ConsListContext): StellaType {
        ctx.updateLocals()

        val headType = ctx.head.accept(this)
        val tailType = ctx.tail.accept(this)!!
        if (tailType !is StellaList) {
            throw TypeCheckingError("ERROR_NOT_A_LIST", "try to cons with no list ${ctx.text}")
        }
        checkType(headType, tailType.elemType, ctx.text)
        return headType
    }

    override fun visitPatternBinding(ctx: PatternBindingContext): StellaType {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
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
        ctx.updateLocals()

        val listType = ctx.list.accept(this)
        if (listType !is StellaList) {
            throw TypeCheckingError("ERROR_NOT_A_LIST", "try to cons with no list ${ctx.text}")
        }
        return listType.elemType
    }


    /**
     * 'List::isempty' '(' list = expr ')'                            # IsEmpty
     */
    override fun visitIsEmpty(ctx: IsEmptyContext): StellaType {
        ctx.updateLocals()

        val listType = ctx.list.accept(this)
        if (listType !is StellaList) {
            throw TypeCheckingError("ERROR_NOT_A_LIST", "try to cons with no list ${ctx.text}")
        }
        return Bool
    }


    /**
     * 'List::tail' '(' list = expr ')'                               # Tail
     */
    override fun visitTail(ctx: TailContext): StellaType {
        ctx.updateLocals()

        val listType = ctx.list.accept(this)
        if (listType !is StellaList) {
            throw TypeCheckingError("ERROR_NOT_A_LIST", "try to cons with no list ${ctx.text}")
        }
        return listType
    }

    override fun visitSucc(ctx: SuccContext): StellaType {
        ctx.updateLocals()

        checkType(Nat, ctx.n)
        return Nat
    }

//    | 'not' '(' expr_ = expr ')'                                     # LogicNot
//    | 'Nat::pred' '(' n = expr ')'                                   # Pred
    /**
     * 'Nat::iszero' '(' n = expr ')'                                 # IsZero
     */
    override fun visitIsZero(ctx: IsZeroContext): StellaType {
        ctx.updateLocals()

        checkType(Nat, ctx.n)
        return Bool
    }
//    | 'fix' '(' expr_ = expr ')'                                     # Fix
//override fun visitFix(ctx: StellaParser.FixContext): StellaType {
//    if (ctx == null) {
//        throw TypeCheckingError.emptyContext()
//    }
//    ctx.updateLocals()
//
//}
//    | 'Nat::rec' '(' n = expr ',' initial = expr ',' step = expr ')' # NatRec
//    | 'fold' '[' type_ = stellatype ']' expr_ = expr                 # Fold
//    | 'unfold' '[' type_ = stellatype ']' expr_ = expr               # Unfold

    /**
     * fun = expr '(' (args += expr (',' args += expr)*)? ')'
     */
    override fun visitApplication(ctx: ApplicationContext): StellaType {
        ctx.updateLocals()

        val funType = ctx.`fun`.accept(this)
        if (funType !is Fun) {
            throw TypeCheckingError("ERROR_NOT_A_FUNCTION", "not a function in application ${ctx.text}")
        }

        if (ctx.args.size != funType.arg.size) {
            throw TypeCheckingError("ERROR_INCORRECT_NUMBER_OF_ARGUMENTS", " at ${ctx.text}")
        }

        funType.arg.zip(ctx.args).stream().forEach { (expType, actCtx) ->
            checkType(expType, actCtx)
        }

        return funType.res
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
        ctx.updateLocals()
        return Fun(acceptAndGet(ctx.paramDecls), ctx.returnExpr.accept(this))
    }


    /**
     * '{' (exprs += expr (',' exprs += expr)*)? '}' # Tuple
     */
    override fun visitTuple(ctx: TupleContext): StellaType {
        ctx.updateLocals()
        return Tuple(ctx.exprs.stream().map { it.accept(this) }.collect(Collectors.toList()))
    }

    /**
     * '{' bindings += binding (',' bindings += binding)* '}' # Record
     */
    override fun visitRecord(ctx: RecordContext): StellaType {
        ctx.updateLocals()

        val items = hashMapOf<String, StellaType>()
        ctx.bindings.stream()
            .forEach {
                it.updateLocals()
                val name: String = it.name.text
                val type: StellaType = it.rhs.accept(this)
                if (items.containsKey(name)) {
                    throw TypeCheckingError("ERROR_RECORD_DUPLICATE_LABEL", "at ${ctx.text}")
                } else items[name] = type
            }
        return Record(items)

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

    override fun visitTypeAsc(ctx: TypeAscContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitNatRec(ctx: NatRecContext): StellaType {
        TODO("Not yet implemented")
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
        ctx.updateLocals()

        checkType(Bool, ctx.condition)
        val resType = ctx.thenExpr.accept(this)
        checkType(resType, ctx.elseExpr)
        return resType
    }

    /**
     * '(' expr_ = expr ')'    # ParenthesisedExpr
     */
    override fun visitParenthesisedExpr(ctx: ParenthesisedExprContext): StellaType {
        ctx.updateLocals()
        return ctx.expr_.accept(this)
    }
}