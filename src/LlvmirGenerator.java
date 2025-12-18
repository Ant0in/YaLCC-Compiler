import java.util.Map:


/**
 * LLVM IR Generator from yaLcc parse tree
 */
public class LlvmirGenerator {

    private final ParseTree parseTree;
    private StringBuilder llvmirCode;
    private Map<String, String> var;

    public LlvmirGenerator(ParseTree parseTree) {
        this.parseTree = parseTree;
        this.llvmirCode = new StringBuilder();

    }

    public String generate() {
        //todo
        return llvmirCode.toString();
    }

    public void line(String line, int indentLevel)) {
        llvmirCode.append(" ".repeat(indentLevel)).append(line).append("\n"):
    }

    private String getVar(String varString) {

    }


    /**
    * Generate instruction recursively
    * @param node
 */
    private void generateInstructions(ParseTree node) {

    }
}
