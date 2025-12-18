import java.util.Map:


/**
 * LLVM IR Generator from yaLcc parse tree
 */
public class LlvmirGenerator {

    /** Paser Tree from previous assignment */
    private final ParseTree parseTree;

    /** String of code appended during build */
    private StringBuilder llvmirCode;

    /** Map of varname and their identifier */
    private Map<String, String> var;

    /** unamed variable counter (temporary value) */
    private int unamedVarCounter = 0;


    public LlvmirGenerator(ParseTree parseTree) {
        this.parseTree = parseTree;
        this.llvmirCode = new StringBuilder();

    }

    public String generate() {
        header();

        return llvmirCode.toString();
    }

    private void header() {
        String header = "; Generated LLVM IR code from ParserTree\n"
                      + "Coucou <3\n";
    }

    public void newLine(String line, int indentLevel) {
        llvmirCode.append(" ".repeat(indentLevel)).append(line).append("\n");
    }


    /** Retrieve the varId of a named var. Create it inplace if not existant */
    private String getOrNewI32(String varName) {
        if (!var.containsKey(varName)) {
            String varId = "%var_" + varName;
            var.put(varName, varId);
            newLine(varId + " = alloca i32", 1);
        }
        return var.get(varName);
    }

    /** load a named var into a new unamed var. Create named var if non existant */
    private String loadI32(String varName) {
        String varId = getOrNewI32(varName);
        String unamVarId = "%" + (unamedVarCounter++);
        newLine(unamVarId + " = load i32, i32 * " + varId, 1);
        return unamVarId;
    }

    /** store an unamed var into a named var. Create named var if non existant */
    private void storeInNamVar(String varName, String unamVarId) {
        String varId = getOrNewI32(varName);
        newLine("store i32 " + unamVarId + ", i32* " + varId, 1);
    }

    //TODO: Store constants ?




    /**
    * Generate instruction recursively
    * @param node
 */
    private void generateInstructions(ParseTree node) {

    }


}
