package org.pl

import grammar.gen.StellaParser
import grammar.gen.StellaParserVisitor
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor

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

    override fun visitGreaterThanOrEqual(ctx: StellaParser.GreaterThanOrEqualContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitLogicNot(ctx: StellaParser.LogicNotContext): StellaType {
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

    override fun visitMultiply(ctx: StellaParser.MultiplyContext): StellaType {
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

    override fun visitFold(ctx: StellaParser.FoldContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitAdd(ctx: StellaParser.AddContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitTypeForAll(ctx: StellaParser.TypeForAllContext): StellaType {
        TODO("Not yet implemented")
    }

    /**
     * name = StellaIdent                                        # TypeVar
     */
    override fun visitTypeVar(ctx: StellaParser.TypeVarContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitSubtract(ctx: StellaParser.SubtractContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitTypeCast(ctx: StellaParser.TypeCastContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitTypeApplication(ctx: StellaParser.TypeApplicationContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitPred(ctx: StellaParser.PredContext): StellaType {
        TODO("Not yet implemented")
    }


    override fun visitUnfold(ctx: StellaParser.UnfoldContext): StellaType {
        TODO("Not yet implemented")
    }


    override fun visitDeclFunGeneric(ctx: StellaParser.DeclFunGenericContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitDeclTypeAlias(ctx: StellaParser.DeclTypeAliasContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitInlineAnnotation(ctx: StellaParser.InlineAnnotationContext): StellaType {
        TODO("Not yet implemented")
    }

    override fun visitTypeRec(ctx: StellaParser.TypeRecContext): StellaType {
        TODO("Not yet implemented")
    }


    override fun visitTypeAbstraction(ctx: StellaParser.TypeAbstractionContext): StellaType {
        TODO("Not yet implemented")
    }


}