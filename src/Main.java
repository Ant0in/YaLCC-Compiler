
import java.io.*;


/**
 * Main class to run the parser for YaLCC.
 * Read a source file, parse it, and print leftmost derivation rule numbers.
 * Additionally, can output LaTeX representation if specified.
 */
public class Main {

    /**
     * Helper class to parse command line arguments.
     */
    private static class ArgParser {

        /** The input source file */
        String inputFile;

        /** The optional LaTeX output file */
        String latexFile;

        /**
         * Parses command line arguments. Will exit on invalid arguments.
         * @param args command line arguments
         */
        ArgParser(String[] args) {

            // 1 argument: source file only
            // 3 arguments: -wt latexFile sourceFile
            // otherwise: error

            if (args.length == 1) {

                inputFile = args[0];
                latexFile = null;

            } else if (args.length == 3 && args[0].equals("-wt")) {

                latexFile = args[1];
                inputFile = args[2];

            } else {

                System.err.println("Invalid arguments. Usage: java -jar part2.jar [-wt filename.tex] sourceFile.ycc");
                System.exit(1);

            }
        }

    }

    /**
     * Main method to run the parser.
     * @param args command line arguments; expects source file and optional -wt for LaTeX output
     */
    public static void main(String[] args) {

        // Parse command line arguments
        ArgParser parsed = new ArgParser(args);
        String inputFile = parsed.inputFile;
        String latexFile = parsed.latexFile;

        // Parse the input file
        ParseTree tree = parseFile(inputFile);

        // handle tree
        if (tree != null) {

            // print leftmost derivation rule numbers
            printRuleNumbers(tree);
            System.out.println();

            // if LaTeX output requested, write to file
            if (latexFile != null) {
                writeLaTeXToFile(tree, latexFile);
            }

        }

    }

    /**
     * Writes the LaTeX representation of the parse tree to a file.
     * @param tree the parse tree
     * @param latexFile the output LaTeX file
     */
    private static void writeLaTeXToFile(ParseTree tree, String latexFile) {

        // create a file and write LaTeX content
        try (FileWriter fw = new FileWriter(latexFile)) {
            fw.write(tree.toLaTeX());
            System.out.println("[i] LaTeX output written to " + latexFile);
        } catch (IOException e) {
            System.err.println("[e] Error writing LaTeX file: " + e.getMessage());
        }

    }

    /**
     * Parses the input file and returns the parse tree.
     * @param filename the source file to parse
     * @return the resulting parse tree
     */
    private static ParseTree parseFile(String filename) {

        ParseTree tree = null;

        // try parsing the file
        try (FileReader fr = new FileReader(filename)) {

            // create lexer and parser, and parse the program
            LexicalAnalyzer lexer = new LexicalAnalyzer(fr);
            Parser parser = new Parser(lexer);
            tree = parser.parseProgram();

        } catch (ParseException pe) {

            // handle parsing errors
            System.err.println("[e] Parse Error at line " + pe.getLine() + ", column " + pe.getColumn() + ": " + pe.getMessage());
            System.err.println("    Expected: " + pe.getExpected() + ", Found: " + pe.getFound());

        } catch (IOException ioe) {

            // handle I/O errors
            System.err.println("[e] I/O Error: " + ioe.getMessage());

        }
    
        return tree;

    }

    /**
     * Recursively prints the rule numbers in leftmost derivation order.
     * @param node the current parse tree node
     */
    private static void printRuleNumbers(ParseTree node) {

        // this works recursively in a pre-order traversal (DFS)
        // if the rule number is valid, print it, then visit children

        if (node.getRuleNumber() != -1) {
            System.out.print(node.getRuleNumber() + " ");
        }

        for (ParseTree child : node.getChildren()) {
            printRuleNumbers(child);
        }

    }

}
