package org.pl

import grammar.gen.StellaParser
import grammar.gen.StellaParserVisitor
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.RuleNode
import org.antlr.v4.runtime.tree.TerminalNode

abstract class TypeCheckingCommonVisitor : StellaParserVisitor<StellaType>, AbstractParseTreeVisitor<StellaType>() {

    override fun visitStart_Expr(ctx: StellaParser.Start_ExprContext?): StellaType {
        return visitChildren(ctx)
    }

    override fun visitStart_Type(ctx: StellaParser.Start_TypeContext?): StellaType {
        return visitChildren(ctx)
    }

    override fun visitStart_Program(ctx: StellaParser.Start_ProgramContext): StellaType {
        return ctx.program().accept(this)
    }
    override fun visitLanguageCore(ctx: StellaParser.LanguageCoreContext?): StellaType {
        return visitChildren(ctx)
    }

    override fun visitAnExtension(ctx: StellaParser.AnExtensionContext?): StellaType {
        return visitChildren(ctx)
    }

    override fun visitGreaterThanOrEqual(ctx: StellaParser.GreaterThanOrEqualContext?): StellaType {
        TODO("Not yet implemented")
    }override fun visitLogicNot(ctx: StellaParser.LogicNotContext?): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitParenthesisedPattern(ctx: StellaParser.ParenthesisedPatternContext?): StellaType {
        TODO("Not yet implemented")
    }
    override fun visitTerminatingSemicolon(ctx: StellaParser.TerminatingSemicolonContext?): StellaType {
        return visitChildren(ctx)
    }

    override fun visitNotEqual(ctx: StellaParser.NotEqualContext?): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitPanic(ctx: StellaParser.PanicContext?): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitLessThanOrEqual(ctx: StellaParser.LessThanOrEqualContext?): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitLogicAnd(ctx: StellaParser.LogicAndContext?): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitLogicOr(ctx: StellaParser.LogicOrContext?): StellaType {
        TODO("Not yet implemented")
    }




}