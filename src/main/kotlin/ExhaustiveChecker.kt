package org.pl

import grammar.StellaRuleContext
import grammar.gen.StellaParser.*

class ExhaustiveChecker(private val matchCtx: MatchContext) {

    private fun destroyParenthes(ctx: StellaRuleContext): StellaRuleContext {
        var clearCtx = ctx
        while (clearCtx is ParenthesisedPatternContext) {
            clearCtx = clearCtx.pattern_
        }
        return clearCtx
    }

    fun check(patterns: List<StellaRuleContext>, patternType: StellaType) {
        val error = TypeCheckingError.nonExhausPattern(matchCtx, patternType)
        if (patterns.isEmpty()) {
            throw error
        }
        val patts = patterns.map { destroyParenthes(it) }
        if (patts.any { it is PatternVarContext }) {
            return
        }
        when (patternType) {
            is Sum -> checkSumExhaustive(patts, patternType)
            is Bool -> checkBoolExhaustive(patts, patternType)
            is Variant -> checkVariantExhaustive(patts, patternType)
            is Record -> checkRecordExhaustive(patts, patternType)
            is Nat -> checkNatExhaustive(patts, patternType)
            is StellaList -> checkListExhaustive(patts, patternType)
            is Tuple -> checkTupleExhaustive(patts, patternType)
            is StellaUnit -> {
                if (patterns.filterIsInstance<PatternUnitContext>().isEmpty()) {
                    throw error
                }
            }

            else -> throw Exception("Unknown type to exhaustive checking: $patternType")
        }
    }


    private fun checkSumExhaustive(patterns: List<StellaRuleContext>, patternType: Sum) {
        val inls = patterns.filterIsInstance<PatternInlContext>()
        val inrs = patterns.filterIsInstance<PatternInrContext>()
        assert(patterns.size == inrs.size + inls.size)
        checkInlExhaustive(inls, patternType)
        checkInrExhaustive(inrs, patternType)
    }

    private fun checkInlExhaustive(ctxs: List<PatternInlContext>, patternType: StellaType) {
        if (ctxs.isEmpty()) {
            throw TypeCheckingError.nonExhausPattern(matchCtx, patternType)
        }
        val type = patternType as Sum
        check(ctxs.map { it.pattern_ }, type.left)
    }

    private fun checkInrExhaustive(ctxs: List<PatternInrContext>, patternType: StellaType) {
        if (ctxs.isEmpty()) {
            throw TypeCheckingError.nonExhausPattern(matchCtx, patternType)
        }
        val type = patternType as Sum
        check(ctxs.map { it.pattern_ }, type.right)
    }

    private fun checkBoolExhaustive(patterns: List<StellaRuleContext>, patternType: Bool) {
        if (patterns.filterIsInstance<PatternTrueContext>().isEmpty()
            || patterns.filterIsInstance<PatternFalseContext>().isEmpty()
        ) {
            throw TypeCheckingError.nonExhausPattern(matchCtx, patternType)
        }
    }

    private fun checkVariantExhaustive(patterns: List<StellaRuleContext>, patternType: Variant) {
        val error = TypeCheckingError.nonExhausPattern(matchCtx, patternType)
        if (patterns.isEmpty()) {
            throw error
        }
        val patts = patterns.filterIsInstance<PatternVariantContext>()
        assert(patts.size == patterns.size)
        val map = patts.groupBy({ it.label.text }, { it.pattern_ })
        if (map.keys != patternType.labelOrder.toSet()) {
            throw error
        }
        for ((label, varPatts) in map) {
            val caseType = patternType.getType(label, error)
            if (caseType != null) {
                check(varPatts, caseType)
            }
        }
    }

    private fun checkRecordExhaustive(patterns: List<StellaRuleContext>, patternType: Record) {
        val error = TypeCheckingError.nonExhausPattern(matchCtx, patternType)
        if (patterns.isEmpty()) {
            throw error
        }
        val cases = patterns.filterIsInstance<PatternRecordContext>()
        assert(cases.size == patterns.size)


        val nameToPatts =
            cases
                .flatMap { rec -> rec.patterns.map { Pair(it.label.text, it.pattern_) } }
                .groupBy({ it.first }, { it.second })
        for ((name, patts) in nameToPatts) {
            check(patts, patternType.items[name]!!)
        }
    }

    private fun checkNatExhaustive(patterns: List<StellaRuleContext>, patternType: Nat) {
        val error = TypeCheckingError.nonExhausPattern(matchCtx, patternType)

        /**
         * |suc --> fund suc(...var) minimal
         * |int
         */
        val numbers = patterns.map { succToInt(it) }
        val minVariable = numbers.filter { !it.isConst }.minOfOrNull { it.value }
        if (minVariable == null) {
            throw error
        }
        if (minVariable == 0) {
            return
        }
        val constants = numbers.filter { it.isConst }.map { it.value }.toSet()
        val neededConstants = generateSequence(0) { it + 1 }.take(minVariable).toSet()
        if (!constants.containsAll(neededConstants)) {
            throw error
        }
    }

    data class IntValue(val isConst: Boolean, val value: Int)

    private fun succToInt(succ: StellaRuleContext): IntValue {
        var cnt = 0
        var ctx: StellaRuleContext = succ
        while (true) {
            when (ctx) {
                is PatternSuccContext -> {
                    ctx = ctx.pattern_
                    cnt++
                }

                is PatternVarContext -> {
                    return IntValue(false, cnt)
                }

                is PatternIntContext -> {
                    return IntValue(true, cnt + ctx.n.text.toInt())
                }

                else -> throw Exception("Unsupported pattern $ctx in PatternSucc")
            }
        }
    }


    data class ListInfo(
        val heads: Sequence<Pair<Int, StellaRuleContext>>,
        val knownLength: Int,
        val isVariableTail: Boolean
    )

    /**
     * This method don't work in common case
     * For example, not fail in test below
     * ```
     * language core;
     *
     * extend with #structural-patterns, #natural-literals, #lists;
     *
     * fn main(n : [Nat]) -> Nat {
     *   return match n {
     *     	[1, x] => 1
     *      |[y, 2] => 3
     *      |[] => 5
     *      |[x] => 4
     *     	| cons(z, cons (x, cons(a, xs))) => 0
     *    }
     * }
     * ```
     *
     * For better algorithm see http://moscova.inria.fr/~maranget/papers/warn/index.html
     */
    private fun checkListExhaustive(patterns: List<StellaRuleContext>, patternType: StellaList) {
        val error = TypeCheckingError.nonExhausPattern(matchCtx, patternType)
        val listCases = patterns.map { getListInfo(it) }
        val shortestVariableListLength = listCases.filter { it.isVariableTail }.minOfOrNull { it.knownLength }
        if (shortestVariableListLength == null) {
            throw error
        }
        if (shortestVariableListLength == 0) {
            return
        }
        val constTailed = listCases.filter { !it.isVariableTail }.map { it.knownLength }
        val neededConstants = generateSequence(0) { it + 1 }.take(shortestVariableListLength).toSet()
        if (!constTailed.containsAll(neededConstants)) {
            throw error
        }
        listCases.flatMap { it.heads }
            .filter { it.first < shortestVariableListLength }
            .groupBy({ it.first }, { it.second })
            .forEach {
                check(it.value, patternType.elemType)
            }
    }

    private fun getListInfo(succ: StellaRuleContext): ListInfo {
        var cnt = 0
        var ctx: StellaRuleContext = succ
        val heads = ArrayList<StellaRuleContext>()
        while (true) {
            when (ctx) {
                is PatternConsContext -> {
                    heads.add(ctx.head)
                    ctx = ctx.tail
                    cnt++
                }

                is PatternListContext -> {
                    for (ptrn in ctx.patterns) {
                        heads.add(ptrn)
                        cnt++
                    }
                    return ListInfo(heads.asSequence().mapIndexed { index, ctx_ -> Pair(index, ctx_) }, cnt, false)
                }

                is PatternVarContext -> {
                    return ListInfo(heads.asSequence().mapIndexed { index, ctx_ -> Pair(index, ctx_) }, cnt, true)
                }

                else -> throw Exception("Unsupported pattern $ctx in PatternSucc")
            }
        }
    }

    private fun checkTupleExhaustive(patterns: List<StellaRuleContext>, patternType: Tuple) {
        val error = TypeCheckingError.nonExhausPattern(matchCtx, patternType)
        if (patterns.isEmpty()) {
            throw error
        }
        val cases = patterns.filterIsInstance<PatternTupleContext>()
        assert(cases.size == patterns.size)
        val listOfTuplesPatterns = cases.map { it.patterns }
        for (idx in 0..<patternType.items.size) {
            val currentPatts = listOfTuplesPatterns.map { it[idx] }
            check(currentPatts, patternType.items[idx])
        }
    }
}