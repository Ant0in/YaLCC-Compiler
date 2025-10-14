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
    /** <code>The</code> */
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
}
