
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Main class to run the lexical analyzer.
 * Read a source file, give it to the lexer and display the tokens and variables.
 */
public class Main {

    /**
     * Main method to run the lexical analyzer.
     * @param args command line arguments; expects a single argument: the source file path.
     */
    public static void main(String[] args) {

        // check for correct number of arguments (1)
        if (args.length < 1) {
            System.out.println("Usage: java -jar part1.jar <source-file>");
            return;
        }
        runLexer(args[0]);
    }

    /**
     * Run the lexical analyzer on the given source file.
     * It prints all tokens and a list of variables with their first occurrence line.
     * @param filePath the path to the source file.
     */
    private static void runLexer(String filePath) {

        try {

            // create lexer and variable map
            LexicalAnalyzer lexer = new LexicalAnalyzer(new FileReader(filePath));
            Map<String, Integer> varMap = new TreeMap<>();
            Symbol token;

            // process tokens until there are none left
            while ((token = lexer.yylex()) != null) {

                if (token.getType() == LexicalUnit.EOS) break;  // end of stream token -> stop processing

                System.out.println(token);
                if (token.getType() == LexicalUnit.VARNAME) {
                    varMap.putIfAbsent(token.getValue().toString(), token.getLine());
                }
            }

            // print variables if any
            if (!varMap.isEmpty()) {
                System.out.println("Variables");
                varMap.forEach((name, line) -> System.out.println(name + " " + line));
            }

        // catch io exceptions (there might be more but this will do for now)
        } catch (IOException e) {
            System.err.println("I/O Error: " + e.getMessage());
        }
    }
}
