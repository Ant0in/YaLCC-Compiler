import java.util.HashMap;

/**
 * LLVM IR Generator from yaLcc parse tree
 */
public class LlvmirGenerator {

    /** Paser Tree from previous assignment */
    private final ParseTree parseTree;

    /** String of code appended during build */
    private StringBuilder llvmirCode;

    /** Map of varname and their identifier */
    private HashMap<String, String> var;

    /** unamed variable counter (temporary value) */
    private int unamedVarCounter = 0;

    public LlvmirGenerator(ParseTree parseTree) {
        this.parseTree = parseTree;
        this.llvmirCode = new StringBuilder();
        this.var = new HashMap<>();
        this.unamedVarCounter = 0;
    }

    private void header() {
        String header =
            "; Generated LLVM IR code from ParserTree\n" + "Coucou <3\n";
    }

    private ParseTree getCodeBranch(ParseTree treeNode) {
        for (ParseTree child : treeNode.getChildren()) {
            if (child.getLabel().isNonTerminal()) {
                return child;
            }
        }
        return null;
    }

    public void generate(ParseTree treeNode) {
        header();
        newLine("define i32 @Prog() {", 0);
        newLine("entry:", 1);

        ParseTree code = getCodeBranch(treeNode);
        if (code != null) {
            code(code);
        }

        newLine("ret i32 0", 1);
        newLine("}", 0);
    }

    /** Recursive method to chenerate code by reading tree */
    private void code(ParseTree treeNode) {
        for (ParseTree child : treeNode.getChildren()) {
            if (child.getLabel().getValue() == NonTerminal.INSTRUCTION) {
                newInstructions(child);
            } else if (child.getLabel().getValue() == NonTerminal.CODE) {
                code(child);
            }
        }
    }

    private void newLine(String line, int indentLevel) {
        llvmirCode.append(" ".repeat(indentLevel)).append(line).append("\n");
    }

    /** Retrieve the varId of a named var. Create it inplace if not existant */
    private String getOrNewI32(String varName) {
        if (!var.containsKey(varName)) {
            String varId = "%var_" + varName;
            var.put(varName, varId);
            newLine(varId + " = alloca i32", 2);
        }
        return var.get(varName);
    }

    /** load a named var into a new unamed var. Create named var if non existant */
    private String loadI32(String varName) {
        String varId = getOrNewI32(varName);
        String unamVarId = "%" + (unamedVarCounter++);
        newLine(unamVarId + " = load i32, i32 * " + varId, 2);
        return unamVarId;
    }

    /** store an unamed var into a named var. Create named var if non existant */
    private void storeInNamI32(String varName, String unamVarId) {
        String varId = getOrNewI32(varName);
        newLine("store i32 " + unamVarId + ", i32* " + varId, 2);
    }

    /**
     * Generate instruction recursively
     * @param node
     */
    private void newInstructions(ParseTree treeNode) {
        ParseTree child = treeNode.getChildren().getFirst();

        switch (child.getLabel().getValue()) {
            case NonTerminal.ASSIGN:
                newAssign(child);
                break;
            case NonTerminal.IF:
                newIf(child);
                break;
            case NonTerminal.WHILE:
                newWhile(child);
                break;
            case NonTerminal.OUTPUT:
                newOutput(child);
                break;
            case NonTerminal.INPUT:
                newInput(child);
                break;
            default:
                throw new IllegalStateException(
                    "Unexpected value: " + child.getLabel().getValue()
                );
        }
    }

    private void newAssign(ParseTree treeNode) {}

    private void newIf(ParseTree treeNode) {}

    private void newWhile(ParseTree treeNode) {}

    private void newOutput(ParseTree treeNode) {}

    private void newInput(ParseTree treeNode) {}

    private void newCond(ParseTree treeNode) {}

    private void newExprArith(ParseTree treeNode) {}
}
