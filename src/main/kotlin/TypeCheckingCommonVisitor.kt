package org.pl

import grammar.gen.StellaParser
import grammar.gen.StellaParserVisitor
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.RuleNode
import org.antlr.v4.runtime.tree.TerminalNode

abstract class TypeCheckingCommonVisitor : StellaParserVisitor<StellaType>, AbstractParseTreeVisitor<StellaType>() {

    override fun visitStart_Expr(ctx: StellaParser.Start_ExprContext): StellaType {
        return visitChildren(ctx)
    }

    override fun visitStart_Type(ctx: StellaParser.Start_TypeContext): StellaType {
        return visitChildren(ctx)
    }

    override fun visitStart_Program(ctx: StellaParser.Start_ProgramContext): StellaType {
        return ctx.program().accept(this)
    }
    override fun visitLanguageCore(ctx: StellaParser.LanguageCoreContext): StellaType {
        return StellaUnit
    }

    override fun visitAnExtension(ctx: StellaParser.AnExtensionContext): StellaType {
        return StellaUnit
    }

    override fun visitGreaterThanOrEqual(ctx: StellaParser.GreaterThanOrEqualContext): StellaType {
        TODO("Not yet implemented")
    }override fun visitLogicNot(ctx: StellaParser.LogicNotContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitParenthesisedPattern(ctx: StellaParser.ParenthesisedPatternContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitDivide(ctx: StellaParser.DivideContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitLessThan(ctx: StellaParser.LessThanContext): StellaType {
        TODO("Not yet implemented")
    }


    override fun visitNotEqual(ctx: StellaParser.NotEqualContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitPanic(ctx: StellaParser.PanicContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitLessThanOrEqual(ctx: StellaParser.LessThanOrEqualContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitLogicAnd(ctx: StellaParser.LogicAndContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitLogicOr(ctx: StellaParser.LogicOrContext): StellaType {
        TODO("Not yet implemented")
    }
    override fun visitGreaterThan(ctx: StellaParser.GreaterThanContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitEqual(ctx: StellaParser.EqualContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitThrow(ctx: StellaParser.ThrowContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitMultiply(ctx: StellaParser.MultiplyContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitAssign(ctx: StellaParser.AssignContext): StellaType {
        TODO("Not yet implemented")
    }


    override fun visitRecordFieldType(ctx: StellaParser.RecordFieldTypeContext): StellaType {
        TODO("Not yet implemented")
    }

    /**
     * label = StellaIdent (':' type_ = stellatype)?;
     */
    override fun visitVariantFieldType(ctx: StellaParser.VariantFieldTypeContext): StellaType {
        TODO("Not yet implemented")
    }


}