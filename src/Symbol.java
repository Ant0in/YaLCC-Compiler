
import java.util.Objects;

/**
 * Symbol objects represent a terminal or non-terminal symbol in the grammar.
 * 
 * @author Not fully determined but assumed to be among Marie Van Den Bogaard, Léo Exibard. Javadoc by Mathieu Sassolas.
 */
public class Symbol {

    /** Undefined line/column position of symbol. */
    private static final int UNDEFINED_POSITION = -1;
    
    /** No value attached to symbol, for terminals without value. */
	private static final Object NO_VALUE = null;

    /** The LexicalUnit (terminal) attached to this token. */
	private final LexicalUnit type;
    
    /** The value attached to the token. May be any Object. In fact, for terminals with value it is indeed the value attached to the terminal. */
	private final Object value;

    /** The position of the symbol in the parsed file. */
	private final int line, column;

    /**
     * Creates a Symbol using the provided attributes.
     * 
     * @param unit the LexicalUnit (terminal) associated with the symbol.
     * @param line the line where the symbol appears in the file.
     * @param column the column where the symbol appears in the file.
     * @param value the value of the symbol.
     */
	public Symbol(LexicalUnit unit, int line, int column, Object value) {
		this.type	= unit;
		this.line	= line + 1;
		this.column	= column;
		this.value	= value;
	}
	
    /**
     * Creates a Symbol using the provided attributes and no value.
     * 
     * @param unit the LexicalUnit (terminal) associated with the symbol.
     * @param line the line where the symbol appears in the file.
     * @param column the column where the symbol appears in the file.
     */
	public Symbol(LexicalUnit unit, int line, int column) {
		this(unit, line, column, NO_VALUE);
	}
	
    /**
     * Creates a Symbol using the provided attributes, without column nor value.
     * 
     * @param unit the LexicalUnit (terminal) associated with the symbol.
     * @param line the line where the symbol appears in the file.
     */
	public Symbol(LexicalUnit unit, int line) {
		this(unit, line, UNDEFINED_POSITION, NO_VALUE);
	}
	
    /**
     * Creates a Symbol using the provided attributes, without position or value.
     * 
     * @param unit the LexicalUnit (terminal) associated with the symbol.
     */
	public Symbol(LexicalUnit unit) {
		this(unit, UNDEFINED_POSITION, UNDEFINED_POSITION, NO_VALUE);
	}
	
    /**
     * Creates a Symbol using the provided attributes, without position.
     * 
     * @param unit the LexicalUnit (terminal) associated with the symbol.
     * @param value the value of the symbol.
     */
	public Symbol(LexicalUnit unit, Object value) {
	    this(unit, UNDEFINED_POSITION, UNDEFINED_POSITION, value);
	}

    /**
     * Returns whether the symbol represents a terminal.
     * 
     * A terminal symbol must have a non-null LexicalUnit type.
     * 
     * @return a boolean which is true iff the Symbol represents a terminal.
     */
	public boolean isTerminal() {
		return this.type != null;
	}
	
    /**
     * Returns whether the symbol represents a non-terminal.
     * 
     * A non-terminal symbol has no type.
     * 
     * @return a boolean which is true iff the Symbol represents a non-terminal.
     */
	public boolean isNonTerminal() {
		return this.type == null;
	}
	
    /**
     * Returns the type of the symbol.
     * 
     * The type of a non-terminal is null.
     * 
     * @return the value of attribute {@link type type}.
     */
	public LexicalUnit getType() {
		return this.type;
	}
	
    /**
     * Returns the value of the symbol.
     * 
     * @return the value of attribute {@link value value}.
     */
	public Object getValue() {
		return this.value;
	}
	
    /**
     * Returns the line where the symbol appeared.
     * 
     * @return the value of attribute {@link line line}.
     */
	public int getLine() {
		return this.line;
	}
	
    /**
     * Returns the column where the symbol appeared.
     * 
     * @return the value of attribute {@link column column}.
     */
	public int getColumn() {
		return this.column;
	}
	
    /**
     * Returns a hash code value for the object.
     * 
     * @return a hash code based on the type and value of the Symbol.
     */
	@Override
	public int hashCode() {
        return Objects.hash(type, value);
	}

    /**
     * Compares this symbol to another object for equality.
     * 
     * Two symbols are equal if they have the same type and value.
     * 
     * @param obj the object to compare with this symbol.
     * @return true if the object is a Symbol with the same type and value, false otherwise.
     */
	@Override
	public boolean equals(Object obj) {

		if (this == obj) return true;  // self check
		if (!(obj instanceof Symbol)) return false;  // type check

		Symbol other = (Symbol) obj;
		return Objects.equals(this.type, other.type) && Objects.equals(this.value, other.value);

	}

    /**
     * Returns a string representation of the symbol.
     * This method has been modified from the provided class to provide the value of the non-terminal symbols.
     * 
     * @return a string representation of the token's value and type.
     */
    @Override
    public String toString() {
        String valueStr = value != null ? value.toString() : "null";
        String typeStr = type != null ? type.toString() : "null";
        return isTerminal()
            ? String.format("token[%s, %s]", typeStr, valueStr)
            : String.format("non-terminal[%s]", valueStr);
    }

    /**
     * Escape LaTeX special characters in a string.
     * If the input string is null, returns an empty string.
     * 
     * @param s the input string.
     * @return the escaped string.
     */
    private static String escapeLaTeX(String s) {

        if (s == null) return "";

        // escape special characters
        return s.replace("\\", "\\textbackslash ")
                .replace("&", "\\&")
                .replace("%", "\\%")
                .replace("$", "\\$")
                .replace("#", "\\#")
                .replace("_", "\\_")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("~", "\\textasciitilde ")
                .replace("^", "\\textasciicircum ");

    }

    /**
     * Returns a LaTeX-formatted string representation of the symbol.
     * 
     * @return a LaTeX-formatted string representation of the symbol.
     */
    public String toTexString() {

        String valueStr = escapeLaTeX(value != null ? value.toString() : "");
        String typeStr = escapeLaTeX(type != null ? type.toString() : "");

        return isTerminal() ? String.format("{\\texttt{%s} {\\textit{%s}}}", typeStr, valueStr)
                           : String.format("{\\textbf{Non Terminal: %s}}", valueStr);

    }
    
}
