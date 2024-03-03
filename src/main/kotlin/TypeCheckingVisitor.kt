package org.pl

import grammar.gen.StellaParser
import grammar.gen.StellaParserBaseVisitor


class TypeCheckingVisitor : StellaParserBaseVisitor<StellaType>() {
    private var isMainMissing: Boolean = true
    private val mainName = "main"

    override fun visitDeclFun(ctx: StellaParser.DeclFunContext?): StellaType {
        if(ctx == null){
            throw TypeCheckingError("Empty AST context", "")
        }
        else {
            if (ctx.name.text == mainName) {
                isMainMissing = false
            }
            // TODO check localDecls
            val expectedType = ctx.returnType.accept(this)
            val actualType = ctx.returnExpr.accept(this)
            if(expectedType != actualType){
                throw TypeCheckingError.unexpectedType(expectedType, actualType, ctx.returnExpr.text)
            }
            return actualType
        }
    }

    override fun visitConstTrue(ctx: StellaParser.ConstTrueContext?): StellaType {
        return Bool
    }

    override fun visitConstFalse(ctx: StellaParser.ConstFalseContext?): StellaType {
        return Bool
    }

    override fun visitTypeBool(ctx: StellaParser.TypeBoolContext?): StellaType {
        return Bool
    }

    override fun visitConstInt(ctx: StellaParser.ConstIntContext?): StellaType {
        return Nat
    }

}