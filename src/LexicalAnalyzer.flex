%%

%class LexicalAnalyzer
%unicode
%public
%line
%column
%type Symbol

%{

private Symbol symbol(LexicalUnit type) {
    return new Symbol(type, yyline, yycolumn);
}

private Symbol symbol(LexicalUnit type, Object value) {
    return new Symbol(type, yyline, yycolumn, value);
}

%}

ID_PROG     = [A-Z][A-Za-z0-9_]*
ID_VAR      = [a-z][A-Za-z0-9_]*
NUMBER      = [0-9]+
WHITESPACE  = [ \t\r\n]+
COMMENT     = "//".*

%%

<YYINITIAL> "Prog"      { return symbol(LexicalUnit.PROG); }
<YYINITIAL> "End"       { return symbol(LexicalUnit.END); }
<YYINITIAL> "="         { return symbol(LexicalUnit.ASSIGN); }
<YYINITIAL> ";"         { return symbol(LexicalUnit.SEMI); }
<YYINITIAL> {NUMBER}    { return symbol(LexicalUnit.NUMBER, Integer.parseInt(yytext())); }
<YYINITIAL> {ID_PROG}   { return symbol(LexicalUnit.PROGNAME, yytext()); }
<YYINITIAL> {ID_VAR}    { return symbol(LexicalUnit.VARNAME, yytext()); }
<YYINITIAL> {WHITESPACE} { /* ignorer */ }
<YYINITIAL> {COMMENT}    { /* ignorer */ }

<YYINITIAL> <<EOF>>      { return symbol(LexicalUnit.EOS); }

. { System.err.println("Unknown character: " + yytext()); }
