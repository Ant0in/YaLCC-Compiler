
import java.io.FileReader;
import java.io.IOException;

/**
 * Main class to run the lexical analyzer.
 * Read a source file, give it to the lexer and display the tokens.
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

            System.out.println("== Tokens ==");

            // read tokens until the end of stream (EOS / null token)
            Symbol token;
            while ((token = lexer.yylex()) != null) {
                if (token.getType() != LexicalUnit.EOS) {
                    System.out.println(token);
                } else {
                    System.out.println("== End of Stream ==");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();  // fallback
        }
    }
}
