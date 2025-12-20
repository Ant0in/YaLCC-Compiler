import java.util.HashMap;
import java.util.List;

/**
 * LLVM IR Generator from yaLcc parse tree
 */
public class LlvmirGenerator {

    /** Top of Paser Tree*/
    private final ParseTree parseTree;

    /** String of code appended during build */
    private StringBuilder llvmirCode;

    /** Map of named var and their identifier */
    private HashMap<String, String> var;

    /** unamed variable counter (temporary value) */
    private int unamedVarCounter = 0;

    /** label counter */
    private int labelCounter = 0;

    /** Used by newLine to indent lines */
    private int indentLevel = 0;

    public LlvmirGenerator(ParseTree parseTree) {
        this.parseTree = parseTree;
        this.llvmirCode = new StringBuilder();
        this.var = new HashMap<>();
    }

    /**
    * Write a indented line in LLVM IR code
    * @param line indent level
    * @param indentLevel
 */
    private void newLine(String line, int indentLevel) {
        llvmirCode.append(" ".repeat(indentLevel)).append(line).append("\n");
    }

    /**
    * Write a indented line in the LLVM IR code.
    * @param line
 */
    private void newLine(String line) {
        newLine(line, indentLevel);
    }

    /** Create new label name. Will not put "%" before the string (important)
     */
    private String getNewLabel() {
        return "label" + labelCounter++;
    }

    /**
    * Create a new unique unamed variable container. "%" in the begining is part of the string as every call nedd it.
    * @return
 */
    private String newUnamedI32Id() {
        return "%" + unamedVarCounter++;
    }


    /**
     * Header write at the begining of the LLVM IR code
     */
    private void header() {
        newLine("Generated LLVM IR code frome ParseTree\n");
        newLine("; External function declarations :");
        newLine("declare i32 @getchar () ; gets one character from stdin");
        newLine("declare i32 @putchar ( i32 ) ; writes one character to stdout");

        newLine("""
        @.strR = private unnamed_addr constant [3 x i8] c"%d\00", align 1

        ; Function Attrs: noinline nounwind optnone ssp uwtable
        define i32 @readInt() #0 {
          %1 = alloca i32, align 4
          %2 = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.strR, i32 0, i32 0), i32* %1)
          %3 = load i32, i32* %1, align 4
          ret i32 %3
        }

        @.strP = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

        define void @println(i32 %x) {
          %1 = alloca i32, align 4
          store i32 %x, i32* %1, align 4
          %2 = load i32, i32* %1, align 4
          %3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.strP, i32 0, i32 0), i32 %2)
          ret void
        }

        declare i32 @printf(i8*, ...)

        declare i32 @scanf(i8*, ...) #1
        """);
    }

    /**
    * Generate LLVM IR code from a ParseTree
    * @param treeNode
 */
    public String generateLLVMIR(ParseTree treeNode) {
        indentLevel = 0;
        header();
        newLine("define i32 @Prog() {");
        newLine("entry:");

        indentLevel++;
        ParseTree codeNode = treeNode.getChildren().get(1);
        if (codeNode != null) {
            newCodeBranch(codeNode);
        }
        newLine("ret i32 0");

        indentLevel--;

        newLine("}", 0);

        return llvmirCode.toString();
    }

    /** Recursive method to chenerate code by reading tree */
    private void newCodeBranch(ParseTree treeNode) {
        for (ParseTree child : treeNode.getChildren()) {
            if (child.getLabel().getValue() == NonTerminal.INSTRUCTION) {
                newInstructions(child);
            } else if (child.getLabel().getValue() == NonTerminal.CODE) {
                newCodeBranch(child);
            }
        }
    }


    /** Retrieve the varId of a named var. Create it inplace if not existant
     * as named var are global, no need to look for scope (and there is a garbage collector).
     */
    private String getOrNewI32(String varName) {
        if (!var.containsKey(varName)) {
            String varId = "%var_" + varName;
            var.put(varName, varId);
            newLine(varId + " = alloca i32");
        }
        return var.get(varName);
    }

    /** load a named var into a new unamed var. Create named var if non existant */
    private String loadI32(String varName) {
        String varId = getOrNewI32(varName);
        String unamedI32Id = newUnamedI32Id();
        newLine(unamedI32Id + " = load i32, i32 * " + varId);
        return unamedI32Id;
    }

    /** store an unamed var into a named var. Create named var if non existant */
    private void storeInNamI32(String varName, String unamedI32Id) {
        String varId = getOrNewI32(varName);
        newLine("store i32 " + unamedI32Id + ", i32* " + varId);
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

    /**
    * Assign a value or ExprArith in a named i32
    * @param treeNode
 */
    private void newAssign(ParseTree treeNode) {
        String varName = null;
        ParseTree exprNode = null;

        for (ParseTree child : treeNode.getChildren()) {
            if (child.getLabel().isTerminal() && child.getLabel().getValue() == LexicalUnit.VARNAME) {
                varName = child.getLabel().getValue().toString();
            } else if (child.getLabel().isNonTerminal() && child.getLabel().getValue() == NonTerminal.EXPR_ARITH) {
                exprNode = child;
            }
        }

        String varId = newExprArith(exprNode);
        storeInNamI32(varName, varId);
    }


    private void newIf(ParseTree treeNode) {
        ParseTree condNode = null;
        ParseTree thenNode = null;
        ParseTree elseNode = null;


        List<ParseTree> children = treeNode.getChildren();
        for (int i = 0; i < children.size(); i++) {
            ParseTree child = children.get(i);
            if (child.getLabel().getValue() == NonTerminal.COND_IMPL) {
                condNode = child;
            } else if ( child.getLabel().getValue() == LexicalUnit.THEN && i + 1 < children.size()) {
                thenNode = children.get(1 + i);
            } else if ( child.getLabel().getValue() == LexicalUnit.ELSE && i + 1 < children.size()) {
                elseNode = children.get(1 + i);
            }
        }

        //jump labels
        String condId = newCond(condNode);
        String thenLabel = getNewLabel();
        String elseLabel = getNewLabel();
        String endLabel = getNewLabel();

        //wite conditional branch
        // only label don't have the "%" in front.
        newLine("br i1 " + condId + ", label %" + thenLabel +  ", label %" + elseLabel);
        newLine(thenLabel + ":");
        indentLevel++;
        newCodeBranch(thenNode);
        newLine("br label %" + endLabel);

        indentLevel--;
        newLine(elseLabel + ":");
        indentLevel++;
        if (elseNode != null) {
            newCodeBranch(elseNode);
        }
        indentLevel--;
        newLine(endLabel);
    }

    /**
    * Create a new cond. Store the result in variable en return the identifier (%).
    * @param treeNode
    * @return
 */
    private String newCond(ParseTree treeNode) {
        List<ParseTree> children = treeNode.getChildren();
        String condId = null;

        if (children.size() == 1) {
            return newCond(children.getFirst());
        } else {

            LexicalUnit type = children.get(1).getLabel().getType();
            //cond1 -> cond1
            if (type == LexicalUnit.IMPLIES) {

            } else if (type == NonTerminal.CO)



            newLine()

        }

        return condId;
    }


    private void newWhile(ParseTree treeNode) {
        ParseTree condNode = null;
        ParseTree codeNode = null;

        List<ParseTree> children = treeNode.getChildren();
        for (int i = 0; i < children.size(); i++) {
            ParseTree child = children.get(i);
            if (child.getLabel().getValue() == NonTerminal.COND_IMPL) {
                condNode = child;
            } else if ( child.getLabel().getType() == LexicalUnit.DO) {
                codeNode = children.get(1 + i);
            }
        }

        newLine("; while");
        String startWhileLabel = getNewLabel();
        String codeWhileLabel = getNewLabel();
        String endWhileLabel = getNewLabel();

        //condition check
        newLine("br label %" + startWhileLabel);
        newLine(startWhileLabel + ":");
        indentLevel++;
        String condId = newCond(condNode);
        newLine("br i1 " + condId + ", label %" + codeWhileLabel + ", label %" + endWhileLabel);
        indentLevel--;

        //while code body
        newLine(codeWhileLabel + ":");
        indentLevel++;
        newCodeBranch(codeNode);
        newLine("br label %" + startWhileLabel);
        indentLevel--;

        //end while
        newLine(endWhileLabel = ":");
    }

    /** Generate output for the grammar:
     * <Output> ==>Print([VarName])
     */
    private void newOutput(ParseTree treeNode) {
        String varName = treeNode.getChildren().getFirst().getLabel().getValue().toString();

        newLine("; print(" + varName + ")");
        String valueId = loadI32(varName);
        newLine("call void @println(i32 " + valueId + ")");
    }

    /** Read a int in input and return the name of the new unamed register holding it.
     * < Input > ==> Input([VarName])
     */
    private String newInput(ParseTree treeNode) {
        String varName = treeNode.getChildren().getFirst().getLabel().getValue().toString();

        newLine("; read(" + varName + ")");
        String valReadId = newUnamedI32Id();
        newLine(valReadId + " = call i32 @readInt()");
        return valReadId;
    }

    private String newExprArith(ParseTree treeNode) {
        ParseTree child = treeNode.getChildren().getFirst();
        LexicalUnit lexi = child.getLabel().getType();


        switch (lexi) {
            case LexicalUnit.VARNAME:
                String varId = child.getLabel().getValue().toString();
                return loadI32(varId);
            case LexicalUnit.NUMBER:
                String number = child.getLabel().getValue().toString();
                String numberId = newUnamedI32Id();

                newLine(numberId + " = add i32 0, " + number);
                return numberId;
            case LexicalUnit.LPAREN:
                ParseTree exprNode = treeNode.getChildren().get(1);
                return newExprArith(treeNode);
            default:
                throw new IllegalStateException(
                    "Unexpected value: " + lexi
                );
        }
    }
}
