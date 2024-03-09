cd src/main/java/grammar
antlr4 -o gen -package grammar.gen -Dlanguage=Java StellaLexer.g4
antlr4 -o gen -package grammar.gen -visitor -no-listener -DcontextSuperClass=grammar.StellaRuleContext  -Dlanguage=Java StellaParser.g4
