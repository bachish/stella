# stella

# Generate parser

1. Generate lexer files (to support correct ANTLR-files highlighting in IDEA)

```
cd src/main/java/grammar 
antlr4 -o gen -package grammar.gen -Dlanguage=Java StellaLexer.g4
```

2. Generate parser files and base visitor class
```
antlr4 -o gen -package grammar.gen -visitor -no-listener -Dlanguage=Java StellaParser.g4
```
