
/**
 * Enumeration of non-terminal symbols used in the grammar.
 * Those are used as descriptors for the ParserTree nodes.
 */
public enum NonTerminal {
    /** Program production */
    PROGRAM,
    /** Code production */
    CODE,
    /** Instruction production */
    INSTRUCTION,
    /** Assignment production */
    ASSIGN,
    /** If production */
    IF,
    /** While production */
    WHILE,
    /** Output production */
    OUTPUT,
    /** Input production */
    INPUT,
    /** Conditional implication production */
    COND_IMPL,
    /** Conditional comparison production */
    COND_COMP,
    /** Conditional atom production */
    COND_ATOM,
    /** Arithmetic expression production */
    EXPR_ARITH,
    /** Addition/Subtraction expression production */
    EXPR_ADDSUB,
    /** Multiplication/Division expression production */
    EXPR_MULDIV,
    /** Unary expression production */
    EXPR_UNARY,
    /** Primary expression production */
    EXPR_PRIMARY,
    /** Unary minus operation */
    UNARY_MINUS
}
