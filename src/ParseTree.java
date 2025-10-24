
import java.util.ArrayList;
import java.util.List;


/* 

LL(1) Parsing Table

Non-Terminal   | Token(s) (Terminal)                        | Production                            | Rule #
---------------|------------------------------------------- |---------------------------------------|---------
Program        | PROG                                       | Prog PROGNAME Is Code End             | 1
               |                                            |                                       |
Code           | VARNAME, IF, WHILE, PRINT, INPUT           | Instruction ; Code                    | 2
Code           | END                                        | ε                                     | 3
               |                                            |                                       |
Instruction    | VARNAME                                    | Assign                                | 4
Instruction    | IF                                         | If                                    | 5
Instruction    | WHILE                                      | While                                 | 6
Instruction    | PRINT                                      | Output                                | 7
Instruction    | INPUT                                      | Input                                 | 8
               |                                            |                                       |
Assign         | VARNAME                                    | VarName = ExprArith                   | 9
               |                                            |                                       |
If             | IF                                         | If { Cond } Then Code End             | 10
If             | IF                                         | If { Cond } Then Code Else Code End   | 11
               |                                            |                                       |
While          | WHILE                                      | While { Cond } Do Code End            | 12
               |                                            |                                       |
Output         | PRINT                                      | Print ( VarName )                     | 13
Input          | INPUT                                      | Input ( VarName )                     | 14
               |                                            |                                       |
Cond           | PIPE, VARNAME, NUMBER, LPAREN, MINUS       | CondImpl                              | 15
               |                                            |                                       |
CondImpl       | PIPE, VARNAME, NUMBER, LPAREN, MINUS       | CondAtom -> CondImpl                  | 16
CondImpl       | PIPE, VARNAME, NUMBER, LPAREN, MINUS       | CondAtom                              | 16'
               |                                            |                                       |
CondAtom       | PIPE                                       | | Cond |                              | 17
CondAtom       | VARNAME, NUMBER, LPAREN, MINUS             | CondComp                              | 18
               |                                            |                                       |
CondComp       | VARNAME, NUMBER, LPAREN, MINUS             | ExprArith Comp ExprArith              | 19
               |                                            |                                       |
ExprArith      | VARNAME, NUMBER, LPAREN, MINUS             | ExprAddSub                            | 20
ExprAddSub     | VARNAME, NUMBER, LPAREN, MINUS             | ExprMulDiv { (+|-) ExprMulDiv }*      | 21
ExprMulDiv     | VARNAME, NUMBER, LPAREN, MINUS             | ExprUnary { (*|/) ExprUnary }*        | 22
ExprUnary      | MINUS                                      | - ExprPrimary                         | 23
ExprUnary      | VARNAME, NUMBER, LPAREN                    | ExprPrimary                           | 24
ExprPrimary    | VARNAME                                    | VarName                               | 25
ExprPrimary    | NUMBER                                     | Number                                | 26
ExprPrimary    | LPAREN                                     | ( ExprArith )                         | 27

*/



/**
 * Represents a parse tree node for YaLCC.
 * It can be a leaf or an internal node with children.
 * Each node has a label (Symbol) and a rule number.
 */
public class ParseTree {

    /** The label symbol for this node */
    private final Symbol label;

    /** The list of child nodes */
    private final List<ParseTree> children;

    /** The rule number used to create this node */
    private int ruleNumber;

    /**
     * Creates a leaf node with a given symbol.
     * This node has no children, and rule number is set to -1.
     * @param lbl the symbol labeling this node
     */
    public ParseTree(Symbol lbl) {
        this.label = lbl;
        this.children = new ArrayList<>();
        this.ruleNumber = -1;
    }

    /**
     * Creates a node with a symbol, a list of children and a rule number.
     * @param lbl the symbol labeling this node
     * @param chdn the list of child nodes
     * @param ruleNum the rule number used to create this node
     */
    public ParseTree(Symbol lbl, List<ParseTree> chdn, int ruleNum) {
        this.label = lbl;
        this.children = chdn;
        this.ruleNumber = ruleNum;
    }

    /**
     * Adds a child node to this node.
     * @param child the child node to add
     */
    public void addChild(ParseTree child) {
        children.add(child);
    }

    /**
     * Returns the list of child nodes
     * @return the list of child nodes
     */
    public List<ParseTree> getChildren() {
        return children;
    }

    /**
     * Returns the rule number
     * @return the rule number
     */
    public int getRuleNumber() {
        return ruleNumber;
    }

    /**
     * Sets the rule number
     * @param ruleNumber the rule number to set
     */
    public void setRuleNumber(int ruleNumber) {
        this.ruleNumber = ruleNumber;
    }

    /**
     * Returns the label symbol
     * @return the label symbol
     */
    public Symbol getLabel() {
        return label;
    }

    /**
     * Writes the tree in LaTeX forest format.
     * @return the tree in LaTeX forest format
     */
    public String toLaTexTree() {

        // forest format: [ {label} child1 child2 ... childN ]
        StringBuilder treeTeX = new StringBuilder();

        // open this node
        treeTeX.append("[");
        treeTeX.append('{').append(label.toTexString()).append('}');
        treeTeX.append(" ");

        for (ParseTree child : children) {
            // recursively add child trees
            treeTeX.append(child.toLaTexTree());
        }

        // close this node
        treeTeX.append("]");

        return treeTeX.toString();

    }

    /**
     * Writes the tree as TikZ code.
     * @return the tree as TikZ code
     */
    public String toTikZ() {

        // TikZ format: node {label} child { child1 } child { child2 } ... child { childN }

        StringBuilder treeTikZ = new StringBuilder();

        // open this node
        treeTikZ.append("node {");
        treeTikZ.append(label.toTexString());
        treeTikZ.append("}\n");

        // recursively add child nodes
        for (ParseTree child : children) {
            treeTikZ.append("child { ");
            treeTikZ.append(child.toTikZ());
            treeTikZ.append(" }\n");
        }

        // close this node and return
        return treeTikZ.toString();

    }

    /**
     * Writes the tree as a TikZ picture. TiKz picture can be used in LaTeX documents.
     * @return the tree as a TikZ picture
     */
    public String toTikZPicture() {
        return "\\begin{tikzpicture}[tree layout]\n\\" + toTikZ() + ";\n\\end{tikzpicture}";
    }


    /**
     * Writes the tree as a forest picture. Returns the tree in forest enviroment
     * using the latex code of the tree
     * @return the tree as a forest picture
     */
    public String toForestPicture() {
        return "\\begin{forest}for tree={rectangle, draw, l sep=20pt}" + toLaTexTree() + ";\n\\end{forest}";
    }

    /**
     * Writes the tree as a LaTeX document which can be compiled using PDFLaTeX.
     * 
     * <br>
     * <br>
     * The result can be used with the command:
     * <pre>
     * pdflatex some-file.tex
     * </pre>
     * 
     * @return the tree as a LaTeX document
     */
    public String toLaTeX() {
        return "\\documentclass[border=5pt]{standalone}\n\n\\usepackage{tikz}\n\\usepackage{forest}\n\n\\begin{document}\n\n"
                + toForestPicture() + "\n\n\\end{document}\n%% Local Variables:\n%% TeX-engine: pdflatex\n%% End:";
    }

}
