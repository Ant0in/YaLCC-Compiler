import java.util.HashMap;
import java.util.List;

/**
 * LLVM IR Generator from yaLcc parse tree
 */
public class LlvmirGenerator {

  /** Top of Paser Tree */
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
   *
   * @param line        string to write in llvm ir code.
   * @param indentLevel indent level of llvm code.
   */
  private void newLine(String line, int indentLevel) {
    llvmirCode.append(" ".repeat(indentLevel)).append(line).append("\n");
  }

  /**
   * Write a indented line in the LLVM IR code.
   *
   * @param line string to write in llvm ir code
   */
  private void newLine(String line) {
    newLine(line, indentLevel);
  }

  /**
   * Create new label name. Will not put "%" before the string (important)
   */
  private String newLabel() {
    return "label" + labelCounter++;
  }

  /**
   * Create a new unique unamed variable container. "%" in the begining is part of
   * the string at every call need it.
   *
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
    newLine(
        "declare i32 @putchar ( i32 ) ; writes one character to stdout");

    // input and output functions
    newLine(
        """
            ; declare output and input functions.
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
   * generate LLVM IR code from a ParseTree root program
   *
   * @param treeNode ParseTree root node.
   * @return String of generated LLVM IR code.
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

  /**
   * Retrieve the varId of a named var. Create it inplace if not existant
   * as named var are global, no need to look for scope (and there is a garbage
   * collector).
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
   *
   * @param node
   */
  private void newInstructions(ParseTree treeNode) {
    ParseTree child = treeNode.getChildren().get(0);

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
            "Unexpected value: " + child.getLabel().getValue());
    }
  }

  /**
   * Assign a value or ExprArith to a named i32
   *
   * @param treeNode
   */
  private void newAssign(ParseTree treeNode) {
    String varName = null;
    ParseTree exprNode = null;

    for (ParseTree child : treeNode.getChildren()) {
      if (child.getLabel().isTerminal() &&
          child.getLabel().getValue() == LexicalUnit.VARNAME) {
        varName = child.getLabel().getValue().toString();
      } else if (child.getLabel().isNonTerminal() &&
          child.getLabel().getValue() == NonTerminal.EXPR_ARITH) {
        exprNode = child;
      }
    }

    String varId = newExprArith(exprNode);
    storeInNamI32(varName, varId);
  }

  /**
   * Strat of an if statement.
   *
   * @param treeNode if PaserTree node.
   */
  private void newIf(ParseTree treeNode) {
    ParseTree condNode = null;
    ParseTree thenNode = null;
    ParseTree elseNode = null;

    List<ParseTree> children = treeNode.getChildren();
    for (int i = 0; i < children.size(); i++) {
      ParseTree child = children.get(i);
      if (child.getLabel().getValue() == NonTerminal.COND_IMPL) {
        condNode = child;
      } else if (child.getLabel().getValue() == LexicalUnit.THEN &&
          i + 1 < children.size()) {
        thenNode = children.get(1 + i);
      } else if (child.getLabel().getValue() == LexicalUnit.ELSE &&
          i + 1 < children.size()) {
        elseNode = children.get(1 + i);
      }
    }

    // jump labels
    String condId = newCond(condNode);
    String thenLabel = newLabel();
    String elseLabel = newLabel();
    String endLabel = newLabel();

    // write conditional branch
    newLine(
        "br i1 " +
            condId +
            ", label %" +
            thenLabel +
            ", label %" +
            elseLabel);
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
   * New condition. Value of the result is stored in a unamed i32.
   *
   * @param treeNode node of Cond ParseTree
   * @return String i32 identifier of the result.
   */
  private String newCond(ParseTree treeNode) {
    //  Con -> cond_impl
    ParseTree child = treeNode.getChildren().get(0); // only one child for Cond.
    return newCondImpl(child);
  }

  /**
   * Create a new cond impl. Store the result in variable en return the identifier
   * (%).
   * 
   * @param treeNode
   * @return the unamed i32 id of the result.
   */
  private String newCondImpl(ParseTree treeNode) {
    List<ParseTree> children = treeNode.getChildren();

    if (treeNode.getLabel().getValue() == NonTerminal.COND_ATOM) {
      // CondImpl => CondAtom
      return newCondAtom(children.get(0));
    } else {
      // CondImpl => CondAtom -> CondImpl
      String leftId = newCond(children.get(0));
      String rightId = newCond(children.get(2));

      newLine("; start of a left -> right");

      String notLeft = newUnamedI32Id();
      newLine(notLeft + " = or i1 " + leftId + ", true ; invert left");

      String implId = newUnamedI32Id();
      newLine(implId + " = or i1 " + notLeft + ", " + rightId + "; (not left) or right");

      return implId;
    }
  }

  private String newCondAtom(ParseTree treeNode) {
    ParseTree child = treeNode.getChildren().get(0);

    if (child.getLabel().getValue() == NonTerminal.COND_IMPL) {
      // COndAtom => | Cond |

    } else {
      // CondAtom => CondComp
      return newCondComp(child);
    }

    return "";
  }

  private String compOpToLlvmOp(String compOp) {
    switch (compOp) {
      case "==":
        return "eq";
      case "<":
        return "slt";
      case "<=":
        return "sle";
      default:
        throw new IllegalStateException("Unknow comparison opeartor: " + compOp);
    }
  }

  /**
   * Write a new comparison in llvm ir.
   *
   * @param treeNode node of the comparison.
   * @return String id of the unamed i32 holding the result.
   */
  private String newCondComp(ParseTree treeNode) {

    List<ParseTree> children = treeNode.getChildren();

    String lefti32Id = null;
    String op = null;
    String righti32Id = null;

    // lest
    lefti32Id = newExprArith(children.get(0));

    // comparison operator
    ParseTree comp = children.get(1);
    LexicalUnit opType = comp.getLabel().getType();

    if (opType == LexicalUnit.EQUAL || opType == LexicalUnit.SMALLER || opType == LexicalUnit.SMALEQ) {
      op = comp.getLabel().getValue().toString();
    }

    // right
    righti32Id = newExprArith(children.get(2));

    String compId = compOpToLlvmOp(op);

    String resultId = newUnamedI32Id();
    newLine("; Comparison");
    newLine(resultId + " = icmp " + compId + " i32 " + lefti32Id + ", " + righti32Id);

    return resultId;
  }

  private void newWhile(ParseTree treeNode) {
    ParseTree condNode = null;
    ParseTree codeNode = null;

    List<ParseTree> children = treeNode.getChildren();
    for (ParseTree child : children) {
      if (child.getLabel().getValue() == NonTerminal.COND_IMPL) {
        condNode = child;
      } else if (child.getLabel().getValue() == NonTerminal.CODE) {
        codeNode = child;
      }
    }

    newLine("; while");
    String startWhileLabel = newLabel();
    String codeWhileLabel = newLabel();
    String endWhileLabel = newLabel();

    // condition check
    newLine("; Condition check");
    newLine("br label %" + startWhileLabel);
    newLine(startWhileLabel + ":");
    indentLevel++;
    String condResultId = newCond(condNode);
    newLine(
        "br i1 " +
            condResultId +
            ", label %" +
            codeWhileLabel +
            ", label %" +
            endWhileLabel);
    indentLevel--;

    // while code body
    newLine(codeWhileLabel + ":");
    indentLevel++;
    newCodeBranch(codeNode);
    newLine("br label %" + startWhileLabel);
    indentLevel--;

    // end while
    newLine(endWhileLabel = ":");
  }

  /**
   * Generate output for the grammar:
   * <Output> ==>Print([VarName])
   */
  private void newOutput(ParseTree treeNode) {
    String varName = treeNode
        .getChildren()
        .getFirst()
        .getLabel()
        .getValue()
        .toString();

    newLine("; print(" + varName + ")");
    String valueId = loadI32(varName);
    newLine("call void @println(i32 " + valueId + ")");
  }

  /**
   * Read a int in input and return the name of the new unamed register holding
   * it.
   * < Input > ==> Input([VarName])
   */
  private String newInput(ParseTree treeNode) {
    String varName = treeNode
        .getChildren()
        .getFirst()
        .getLabel()
        .getValue()
        .toString();

    newLine("; read(" + varName + ")");
    String valReadId = newUnamedI32Id();
    newLine(valReadId + " = call i32 @readInt()");
    return valReadId;
  }

  /**
   * Write a new expr arithmetic in llvm ir.
   *
   * @param treeNode ParseTree node of the expr arithm.
   * @return String id of the unamed i32 holding the result.
   */
  private String newExprArith(ParseTree treeNode) {
    ParseTree child = treeNode.getChildren().get(0);
    newLine("; new ExppArith");
    indentLevel++;
    return newExprAddSub(child);
    indentLevel--;

  }

  /**
   * Write a new expr arithmetic add or sub in llvm ir.
   * ExprAddSub => ExprMulDiv { (+|-) ExprMulDiv }*
   *
   * @param treeNode ParseTree node of the ExprAddSub
   * @return String of the unamed i32 id containing the result.
   */
  private String newExprAddSub(ParseTree treeNode) {
    List<ParseTree> children = treeNode.getChildren();
    String resultId = newExprMulDiv(children.get(0));

    for (int i = 1; i < children.size(); i += 2) {
      LexicalUnit op = children.get(i).getLabel().getType();

      String newExprMulDiv = newExprMulDiv(children.get(i + 1));
      String newResultId = newUnamedI32Id();

      if (op == LexicalUnit.PLUS) {
        newLine(newResultId + " = i32 " + resultId + ", " + newExprMulDiv);
      } else if (op == LexicalUnit.MINUS) {
        newLine(newResultId + " = sub i32 " + resultId + ", " + newExprMulDiv);
      }
      resultId = newResultId;
    }
    return resultId;
  }

  private String newExprMulDiv(ParseTree treeNode) {
    List<ParseTree> children = treeNode.getChildren();
    String resultId = newExprUnary(children.get(0));

    for (int i = 1; i < children.size(); i += 2) {
      LexicalUnit op = children.get(i).getLabel().getType();

      String newExprUnary = newExprUnary(children.get(i + 1));
      String newResultId = newUnamedI32Id();

      if (op == LexicalUnit.TIMES) {
        newLine(newResultId + " = mul i32 " + resultId + ", " + newExprUnary);
      } else if (op == LexicalUnit.DIVIDE) {
        newLine(newResultId + " = sdiv i32 " + resultId + ", " + newExprUnary);
      }

      resultId = newResultId;
    }
    return resultId;
  }

  private String newExprUnary(ParseTree treeNode) {
    List<ParseTree> children = treeNode.getChildren();

    if (children.get(0).getLabel().getType() == LexicalUnit.MINUS) {
      String newExprPrimary = newExprPrimary(children.get(1));
      String resultId = newUnamedI32Id();
      newLine(resultId + " = sub i32 0, " + newExprPrimary);
      return resultId;
    } else {
      return newExprPrimary(children.get(0));
    }

  }

  private String newExprPrimary(ParseTree treeNode) {
    ParseTree child = treeNode.getChildren().getFirst();
    LexicalUnit lexi = child.getLabel().getType();

    switch (lexi) {
      case VARNAME:
        String varId = child.getLabel().getValue().toString();
        return loadI32(varId);
      case NUMBER:
        String number = child.getLabel().getValue().toString();
        String numberId = newUnamedI32Id();

        newLine(numberId + " = add i32 0, " + number);
        return numberId;
      case LPAREN:
        ParseTree exprNode = treeNode.getChildren().get(1);
        return newExprArith(exprNode);
      default:
        throw new IllegalStateException("Unexpected value: " + lexi);
    }
  }

  // private String newExprArith(ParseTree treeNode) {
  // ParseTree child = treeNode.getChildren().getFirst();
  // LexicalUnit lexi = child.getLabel().getType();

  // switch (lexi) {
  // case LexicalUnit.VARNAME:
  // String varId = child.getLabel().getValue().toString();
  // return loadI32(varId);
  // case LexicalUnit.NUMBER:
  // String number = child.getLabel().getValue().toString();
  // String numberId = newUnamedI32Id();

  // newLine(numberId + " = add i32 0, " + number);
  // return numberId;
  // case LexicalUnit.LPAREN:
  // ParseTree exprNode = treeNode.getChildren().get(1);
  // return newExprArith(treeNode);
  // default:
  // throw new IllegalStateException("Unexpected value: " + lexi);
  // }
  // }
}
