/**
 * A terminal symbol, a.k.a. a letter in the grammar.
 */
public enum LexicalUnit{
   /** [ProgName] */
    PROGNAME,
    /** [VarName] */
    VARNAME,
    /** [Number] */
    NUMBER,
    /** <code>Prog</code> */
    PROG,
    /** <code>Is</code> */
    IS,
    /** <code>End</code> */
    END,
    /** <code>;</code> */
    SEMI,
    /** <code>=</code> */
    ASSIGN,
    /** <code>(</code> */
    LPAREN,
    /** <code>)</code> */
    RPAREN,
    /** <code>-</code> */
    MINUS,
    /** <code>+</code> */
    PLUS,
    /** <code>*</code> */
    TIMES,
    /** <code>/</code> */
    DIVIDE,
    /** <code>If</code> */
    IF,
    /** <code>Then</code> */
    THEN,
    /** <code>Else</code> */
    ELSE,
    /** <code>{</code> */
    LBRACK,
    /** <code>}</code> */
    RBRACK,
    /** <code>-></code> */
    IMPLIES,
    /** <code>|</code> */
    PIPE,
    /** <code>==</code> */
    EQUAL,
    /** <code>&lt;=</code> */
    SMALEQ,
    /** <code>&lt;</code> */
    SMALLER,
    /** <code>While</code> */
    WHILE,
    /** <code>Do</code> */
    DO,
    /** <code>Print</code> */
    PRINT,
    /** <code>Input</code> */
    INPUT,
    /** End Of Stream */
    EOS, // End of stream
    /** &epsilon; */
    EPSILON; // Epsilon: not actually scanned but detected by the parser

    /**
     * Returns the representation the terminal.
     * 
     * @return a String containing the terminal type (word or abstract expression).
     */
     @Override
    public String toString() {
        String n=this.name();
        switch (this) {
            case PROGNAME:
                n="[ProgName]";
                break;
            case VARNAME:
                n="[VarName]";
                break;
            case NUMBER:
                n="[Number]";
                break;
            case PROG:
                n="Prog";
                break;
            case IS:
                n="Is";
                break;
            case END:
                n="End";
                break;
            case SEMI:
                n=";";
                break;
            case ASSIGN:
                n="=";
                break;
            case LPAREN:
                n="(";
                break;
            case RPAREN:
                n=")";
                break;
            case MINUS:
                n="-";
                break;
            case PLUS:
                n="+";
                break;
            case TIMES:
                n="*";
                break;
            case DIVIDE:
                n="/";
                break;
            case IF:
                n="If";
                break;
            case THEN:
                n="Then";
                break;
            case ELSE:
                n="Else";
                break;
            case LBRACK:
                n="{";
                break;
            case RBRACK:
                n="}";
                break;
            case IMPLIES:
                n="->";
                break;
            case PIPE:
                n="|";
                break;
            case EQUAL:
                n="=";
                break;
            case SMALLER:
                n="<";
                break;
            case SMALEQ:
                n="<=";
                break;
            case WHILE:
                n="While";
                break;
            case DO:
                n="Do";
                break;
            case PRINT:
                n="Print";
                break;
            case INPUT:
                n="Input";
                break;
            case EOS:
                n="EOS";
                break;
            case EPSILON:
                n="/epsilon/";
                break;
        }
        return n;
    }
    
    
    /**
     * Returns the LaTeX code representing the terminal.
     * 
     * @return a String containing the LaTeX code for the terminal.
     */
    public String toTexString() {
        String n=this.name();
        switch (this) {
            case PROGNAME:
                n="ProgName";
                break;
            case VARNAME:
                n="VarName";
                break;
            case NUMBER:
                n="Number";
                break;
            case PROG:
                n="\\texttt{Prog}";
                break;
            case IS:
                n="\\texttt{Is}";
                break;
            case END:
                n="\\texttt{End}";
                break;
            case SEMI:
                n="\\texttt{;}";
                break;
            case ASSIGN:
                n="\\texttt{=}";
                break;
            case LPAREN:
                n="\\texttt{(}";
                break;
            case RPAREN:
                n="\\texttt{)}";
                break;
            case MINUS:
                n="\\texttt{-}";
                break;
            case PLUS:
                n="\\texttt{+}";
                break;
            case TIMES:
                n="\\texttt{*}";
                break;
            case DIVIDE:
                n="\\texttt{/}";
                break;
            case IF:
                n="\\texttt{if}";
                break;
            case THEN:
                n="\\texttt{then}";
                break;
            case ELSE:
                n="\\texttt{else}";
                break;
            case LBRACK:
                n="\\texttt{\\{}";
                break;
            case RBRACK:
                n="\\texttt{\\}}";
                break;
            case IMPLIES:
                n="\\texttt{->}";
                break;
            case PIPE:
                n="\\texttt{|}";
                break;
            case EQUAL:
                n="\\texttt{=}";
                break;
            case SMALEQ:
                n="\\texttt{<=}";
                break;
            case SMALLER:
                n="\\texttt{<}";
                break;
            case WHILE:
                n="\\texttt{While}";
                break;
            case DO:
                n="\\texttt{Do}";
                break;
            case PRINT:
                n="\\texttt{Print}";
                break;
            case INPUT:
                n="\\texttt{Input}";
                break;
            case EOS:
                n="EOS";
                break;
            case EPSILON:
                n="$\\varepsilon$";
                break;
        }
        return n;
    }
}
