package org.pl

import grammar.StellaRuleContext
import grammar.gen.StellaParser

class ExhaustiveChecker(val matchCtx: StellaParser.MatchContext) {

    private fun destroyParenthes(ctx: StellaRuleContext): StellaRuleContext {
        var clearCtx = ctx
        while (clearCtx is StellaParser.ParenthesisedPatternContext) {
            clearCtx = clearCtx.pattern_
        }
        return clearCtx
    }

    fun check(patterns: List<StellaRuleContext>, patternType: StellaType) {
        if (patterns.isEmpty()) {
            throw TypeCheckingError.nonExhausPattern(matchCtx, patternType)
        }
        val patts = patterns.map { destroyParenthes(it) }
        if (patts.any { it is StellaParser.PatternVarContext }) {
            return
        }
        when (patternType) {
            is Sum -> checkSumExhaustive(patts, patternType)
            is Bool -> checkBoolExhaustive(patts, patternType)
            else -> throw TypeCheckingError.nonExhausPattern(matchCtx, patternType)
        }
    }


    private fun checkSumExhaustive(patterns: List<StellaRuleContext>, patternType: StellaType) {
        val inls = patterns.filterIsInstance<StellaParser.PatternInlContext>()
        val inrs = patterns.filterIsInstance<StellaParser.PatternInrContext>()
        assert(patterns.size == inrs.size + inls.size)
        checkInlExhaustive(inls, patternType)
        checkInrExhaustive(inrs, patternType)
    }

    private fun checkInlExhaustive(ctxs: List<StellaParser.PatternInlContext>, patternType: StellaType) {
        if (ctxs.isEmpty()) {
            throw TypeCheckingError.nonExhausPattern(matchCtx, patternType)
        }
        val type = patternType as Sum
        check(ctxs.map { it.pattern_ }, type.left)
    }

    private fun checkInrExhaustive(ctxs: List<StellaParser.PatternInrContext>, patternType: StellaType) {
        if (ctxs.isEmpty()) {
            throw TypeCheckingError.nonExhausPattern(matchCtx, patternType)
        }
        val type = patternType as Sum
        check(ctxs.map { it.pattern_ }, type.right)
    }

    private fun checkBoolExhaustive(patterns: List<StellaRuleContext>, patternType: StellaType) {
        if (patterns.filterIsInstance<StellaParser.PatternTrueContext>().isEmpty()
            || patterns.filterIsInstance<StellaParser.PatternFalseContext>().isEmpty()
        ) {
            throw TypeCheckingError.nonExhausPattern(matchCtx, patternType)
        }
    }

}