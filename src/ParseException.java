import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Exception to be raised when the parsing fails.
 * 
 * @author Not fully determined but assmed to be among Marie Van Den Bogaard, Léo Exibard, Gilles Geeraerts. Javadoc by Mathieu Sassolas.
 */
public class ParseException extends Exception {
    /**
     * Current start of the word (look-ahead) when the error happened.
     */
    private Symbol token;
    
    /**
     * Symbol at the top of the stack when the error happened.
     */
    private NonTerminal variable;
    
    /**
     * List of possible terminal symbols allowed as look-ahead.
     */
    private List<LexicalUnit> alternatives;

    /**
     * Raises an exception on the given look-ahead.
     * 
     * @param symbol the token on which the error happened.
     */
    public ParseException(Symbol symbol){
        this.token = symbol;
        this.alternatives = new ArrayList<LexicalUnit>();
    }

    /**
     * Raises an exception on the given look-ahead and stack top.
     * 
     * @param symbol the token on which the error happened.
     * @param var the (non-terminal) top of the stack when the error happened.
     */
    public ParseException(Symbol symbol, NonTerminal var){
        this.token = symbol;
        this.variable = var;
        this.alternatives = new ArrayList<LexicalUnit>();
    }

    /**
     * Raises an exception on the given look-ahead.
     * 
     * @param symbol the token on which the error happened.
     * @param alts the list of expected terminals.
     */
    public ParseException(Symbol symbol, List<LexicalUnit> alts){
        this.token = symbol;
        this.alternatives = alts;
    }

    /**
     * Raises an exception on the given look-ahead and stack top.
     * 
     * @param symbol the token on which the error happened.
     * @param var the (non-terminal) top of the stack when the error happened.
     * @param alts the list of expected terminals.
     */
    public ParseException(Symbol symbol, NonTerminal var, List<LexicalUnit> alts){
        this.token = symbol;
        this.variable = var;
        this.alternatives = alts;
    }

    /**
     * Joins the list of expected terminals in a readable way.
     * 
     * @return a String containing the list of expected terminals (with English fillers).
     */
    private String stringOfAlternatives(){
        StringBuilder altString = new StringBuilder();
        // We want a special condition for the last element
        if (alternatives.isEmpty()) {
            return "";
        } else {
            altString.append("expected ");
            boolean first = true;
            for (LexicalUnit term: alternatives) {
                if (first) {
                    first = false;
                } else {
                    altString.append(", ");
                }
                altString.append(term);
            }
            altString.append(", but got ");
            return altString.toString();
        }
    }

    /**
     * Returns the detailed message string of this exception.
     * 
     * @return the String message (in English) explaining the parsing error.
     */
    @Override
    public String getMessage(){
        if (variable == null) {
            return String.format("Parsing Error at line %d and column %d: %s%s", token.getLine(), token.getColumn(), stringOfAlternatives(), token.getValue());
        } else {
            return String.format("Parsing Error at line %d and column %d trying to parse %s: %s%s", token.getLine(), token.getColumn(), variable.toString(), stringOfAlternatives(), token.getValue().toString());
        }
    }
}
