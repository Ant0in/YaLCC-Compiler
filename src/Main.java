
import java.io.*;


/**
 * Main class to run the IR Generator for YaLCC.
 * Read a source file, parse it, and print the LLVM IR generated code.
 */
public class Main {

    /**
     * Main method to run the parser.
     * @param args command line arguments; expects source file only.
     */
    public static void main(String[] args) {

        // parse according to part 3 (bye bye my clean parser)
        if (args.length != 1) {
            System.err.println("Usage: java -jar part3.jar sourceFile.ycc");
            System.exit(1);
        }

        String inputFile = args[0];
        ParseTree tree = parseFile(inputFile);

        if (tree == null) {
            System.exit(1); // parsing failed
        }

        // debug print to help me out duh
        System.out.println(tree.toLaTeX());

        // generate the IR
        LLVMIRGenerator gen = new LLVMIRGenerator();
        String llvmCode = gen.generateLLVMIR(tree);

        // display the llvm IR string
        System.out.println(llvmCode);

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

}
