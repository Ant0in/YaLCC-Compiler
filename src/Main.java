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
     * @param args command line arguments; expects a single
     *             argument: the source file path.
     */
    public static void main(String[] args) {

        // check for correct usage
        if (args.length < 1) {
            System.out.println("Usage: java -jar part1.jar <source-file>");
            return;
        }

        String fp = args[0];

        try {

            // create a lexer on the provided file
            LexicalAnalyzer lexer = new LexicalAnalyzer(new FileReader(fp));

            // store first occurrence of each variable in alphabetical order
            Map<String, Integer> varMap = new TreeMap<>();
            Symbol token;

            // read tokens (until EOS / null is returned)
            while ((token = lexer.yylex()) != null) {
                
                // stop on eos
                if (token.getType() == LexicalUnit.EOS) break;

                System.out.println(token);

                // record the line of the first occurrence
                if (token.getType() == LexicalUnit.VARNAME) {
                    varMap.putIfAbsent(token.getValue().toString(), token.getLine());
                }
            }

            // display variables and their first occurrence line
            if (!varMap.isEmpty()) {
                System.out.println("Variables");
                for (Map.Entry<String, Integer> entry : varMap.entrySet()) {
                    System.out.println(entry.getKey() + " " + entry.getValue());
                }
            }

        } catch (IOException e) {
            e.printStackTrace(); // fallback
        }
    }
}
