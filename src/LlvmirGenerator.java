import java.util.HashMap;
import java.util.List

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

    /** label counter */
    private int labelCounter = 0;

    public LlvmirGenerator(ParseTree parseTree) {
        this.parseTree = parseTree;
        this.llvmirCode = new StringBuilder();
        this.var = new HashMap<>();
        this.unamedVarCounter = 0;
    }

    private String newLabel() {
        return "label" + labelCounter++;
    }

    private void header() {
        newline("Generated LLVM IR code frome ParseTree\n", 0);
        newLine("; External function declarations :", 0);
        newLine("declare i32 @getchar () ; gets one character from stdin", 0);
        newLine("declare i32 @putchar ( i32 ) ; writes one character to stdout"0);
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
        String unamedVarId = "%" + unamedVarCounter++;
        newLine(unamedVarId + " = load i32, i32 * " + varId, 2);
        return unamedVarId;
    }

    /** store an unamed var into a named var. Create named var if non existant */
    private void storeInNamI32(String varName, String unamedVarId) {
        String varId = getOrNewI32(varName);
        newLine("store i32 " + unamedVarId + ", i32* " + varId, 2);
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

    private void newAssign(ParseTree treeNode) {
        String varName = null;
        ParseTree exprNode = null;

        for (ParseTree child : treeNode.getChildren()) {
            if (child.getLabel().isTerminal() && child.getLabel().getValue() == LexicalUnit.VARNAME) {
                varName = child.getLabel().getValue().toString();
            } else if (child.getLabel().isNonTerminal() && child.getLabel().getValue() == NonTerminal.EXPR_ARITH) {
                exprNode = child; // TODO: generate expr and get unamed var
            }
        }

        String varId = newExprArith(exprNode);
        storeInNamI32(varName, varId);
    }


    private void newIf(ParseTree treeNode) {
        ParseTree condNode = null;
        ParseTree thenNode = null;
        ParseTree elseNode = null;


        List<ParseTree> childs = treeNode.getChildren();
        for (int i = 0; i < childs.size(); i++) {
            ParseTree child = childs.get(i);
            if (child.getLabel().getValue() == NonTerminal.COND_IMPL) {
                condNode = child;
            } else if ( child.getLabel().getValue() == LexicalUnit.THEN && i + 1 < childs.size()) {
                thenNode = childs.get(1 + i);
            } else if ( child.getLabel().getValue() == LexicalUnit.ELSE && i + 1 < childs.size()) {
                elseNode = childs.get(1 + i);
            }
        }

        String condId = newCond(condNode);
        String thenLabel = nextLabel();
        String alseLabel = nextLabel();
        String endLabel = nextLabel();

        line("br i1 " + condReg + ", label %" + thennLabel +  )
    }



    private void newWhile(ParseTree treeNode) {}

    private void newOutput(ParseTree treeNode) {}

    private void newInput(ParseTree treeNode) {}

    private void newCond(ParseTree treeNode) {}

    private String newExprArith(ParseTree treeNode) {}
}
