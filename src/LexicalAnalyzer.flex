%%

%class LexicalAnalyzer
%unicode
%public
%line
%column
%type Symbol

%{

private Symbol symbol(LexicalUnit type) {
    return new Symbol(type, yyline, yycolumn, yytext());
}

private Symbol symbol(LexicalUnit type, Object value) {
    return new Symbol(type, yyline, yycolumn, value);
}

%}

/* Regex */
ID_PROG        = [A-Z][A-Za-z0-9_]*
ID_VAR         = [a-z][A-Za-z0-9_]*
NUMBER         = [0-9]+
WHITESPACE     = [ \t\r\n]+
SHORT_COMMENT  = \$.*
LONG_COMMENT   = \!\!([^!]|!\+[^!])*?\!\!

%%

/* Keywords and symbols */
<YYINITIAL> "Prog"       { return symbol(LexicalUnit.PROG); }
<YYINITIAL> "Is"         { return symbol(LexicalUnit.IS); }
<YYINITIAL> "End"        { return symbol(LexicalUnit.END); }
<YYINITIAL> "If"         { return symbol(LexicalUnit.IF); }
<YYINITIAL> "Then"       { return symbol(LexicalUnit.THEN); }
<YYINITIAL> "Else"       { return symbol(LexicalUnit.ELSE); }
<YYINITIAL> "While"      { return symbol(LexicalUnit.WHILE); }
<YYINITIAL> "Do"         { return symbol(LexicalUnit.DO); }
<YYINITIAL> "Print"      { return symbol(LexicalUnit.PRINT); }
<YYINITIAL> "Input"      { return symbol(LexicalUnit.INPUT); }

<YYINITIAL> "="           { return symbol(LexicalUnit.ASSIGN); }
<YYINITIAL> ";"           { return symbol(LexicalUnit.SEMI); }
<YYINITIAL> "("           { return symbol(LexicalUnit.LPAREN); }
<YYINITIAL> ")"           { return symbol(LexicalUnit.RPAREN); }
<YYINITIAL> "{"           { return symbol(LexicalUnit.LBRACK); }
<YYINITIAL> "}"           { return symbol(LexicalUnit.RBRACK); }

<YYINITIAL> "+"           { return symbol(LexicalUnit.PLUS); }
<YYINITIAL> "-"           { return symbol(LexicalUnit.MINUS); }
<YYINITIAL> "*"           { return symbol(LexicalUnit.TIMES); }
<YYINITIAL> "/"           { return symbol(LexicalUnit.DIVIDE); }

<YYINITIAL> "=="          { return symbol(LexicalUnit.EQUAL); }
<YYINITIAL> "<="          { return symbol(LexicalUnit.SMALEQ); }
<YYINITIAL> "<"           { return symbol(LexicalUnit.SMALLER); }
<YYINITIAL> "->"          { return symbol(LexicalUnit.IMPLIES); }
<YYINITIAL> "|"           { return symbol(LexicalUnit.PIPE); }

/* ids and numbers */
<YYINITIAL> {NUMBER}      { return symbol(LexicalUnit.NUMBER, Integer.parseInt(yytext())); }
<YYINITIAL> {ID_PROG}     { return symbol(LexicalUnit.PROGNAME, yytext()); }
<YYINITIAL> {ID_VAR}      { return symbol(LexicalUnit.VARNAME, yytext()); }

/* spaces and comments (ignored) */
<YYINITIAL> {WHITESPACE}      { }
<YYINITIAL> {SHORT_COMMENT}   { }
<YYINITIAL> {LONG_COMMENT}    { }

/* end of file */
<YYINITIAL> <<EOF>>      { return symbol(LexicalUnit.EOS); }

/* fallback for unknown characters */
. { throw new RuntimeException("Unknown character at line " + (yyline+1) + ", column " + (yycolumn+1) + ": '" + yytext() + "'"); }
