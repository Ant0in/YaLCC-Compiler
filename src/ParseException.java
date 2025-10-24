
/**
 * Exception thrown when a parsing error occurs.
 * Includes details about the error location and expected vs found tokens.
 */
public class ParseException extends Exception {

    /** Line number where the error occurred */
    private final int line;

    /** Column number where the error occurred */
    private final int column;

    /** The expected lexical unit */
    private final LexicalUnit expected;

    /** The found lexical unit */
    private final LexicalUnit found;

    /**
     * Constructs a ParseException with detailed information.
     * @param message the error message
     * @param line the line number of the error
     * @param column the column number of the error
     * @param expected the expected lexical unit
     * @param found the found lexical unit
     */
    public ParseException(String message, int line, int column, LexicalUnit expected, LexicalUnit found) {
        super(message);
        this.line = line;
        this.column = column;
        this.expected = expected;
        this.found = found;
    }

    /**
     * Returns the line number of the error.
     * @return the line number
     */
    public int getLine() { return line; }

    /**
     * Returns the column number of the error.
     * @return the column number
     */
    public int getColumn() { return column; }

    /**
     * Returns the expected lexical unit.
     * @return the expected lexical unit
     */
    public LexicalUnit getExpected() { return expected; }

    /**
     * Returns the found lexical unit.
     * @return the found lexical unit
     */
    public LexicalUnit getFound() { return found; }

}
