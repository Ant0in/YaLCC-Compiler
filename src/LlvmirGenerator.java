import java.util.Map:


/**
 * LLVM IR Generator from yaLcc parse tree
 */
public class LlvmirGenerator {

    /** Paser Tree from previous assignment */
    private final ParseTree parseTree;

    /** String of code appended during build */
    private StringBuilder llvmirCode;

    /** Map of varname to var identifier */
    private Map<String, String> var;

    /** Counter of indentifier */
    private int identifierCounter = 0;


    public LlvmirGenerator(ParseTree parseTree) {
        this.parseTree = parseTree;
        this.llvmirCode = new StringBuilder();

    }

    public String generate() {
        header();

        return llvmirCode.toString();
    }

    public void line(String line, int indentLevel)) {
        llvmirCode.append(" ".repeat(indentLevel)).append(line).append("\n"):
    }

    /** Generate or retrieve var identifier from name */
    private String getVar(String varName) {
        if (!var.containsKey(varName)) {
            String identifier = "%var_" + varName;
            var.put(varName, identifier);
        }
        return var.get(varName);
    }


    /**
    * Generate instruction recursively
    * @param node
 */
    private void generateInstructions(ParseTree node) {

    }


    private void header() {
        String header = "; Generated LLVM IR code from ParserTree\n"
                      + "Coucou <3\n";
    }
}
