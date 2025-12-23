import java.util.List;
import java.util.ArrayList;

/**
 * A skeleton class to represent parse trees.
 * 
 * The arity is not fixed: a node can have 0, 1 or more children.
 * Trees are represented in the following way: Tree :== Symbol * List&lt;Tree&gt;
 * In other words, trees are defined recursively: A tree is a root (with a label of type Symbol) and a list of trees children. Thus, a leaf is simply a tree with no children (its list of children is empty). This class can also be seen as representing the Node of a tree, in which case a tree is simply represented as its root.
 * 
 * @author Léo Exibard, Sarah Winter, edited by Mathieu Sassolas
 */

public class ParseTree {
    /**
     * The label of the root of the tree.
     */
    private Symbol label;
    
    /**
     * The list of childrens of the root node, which are trees themselves.
     */
    private List<ParseTree> children;

    /**
     * Creates a singleton tree with only a root labeled by lbl.
     * 
     * @param lbl The label of the root
     */
    public ParseTree(Symbol lbl) {
        this.label = lbl;
        this.children = new ArrayList<ParseTree>(); // This tree has no children
    }
    
    /**
     * Creates a singleton tree with only a root labeled by terminal lbl.
     * 
     * @param lbl The label of the root
     */
    public ParseTree(LexicalUnit lbl) {
        this.label = new Symbol(lbl);
        this.children = new ArrayList<ParseTree>(); // This tree has no children
    }
    
    /**
     * Creates a singleton tree with only a root labeled by variable lbl.
     * 
     * @param lbl The label of the root
     */
    public ParseTree(NonTerminal lbl) {
        this.label = new Symbol(null,lbl);
        this.children = new ArrayList<ParseTree>(); // This tree has no children
    }

    /**
     * Creates a tree with root labeled by lbl and children chdn.
     * 
     * @param lbl  The label of the root
     * @param chdn Its children
     */
    public ParseTree(Symbol lbl, List<ParseTree> chdn) {
        this.label = lbl;
        this.children = chdn;
    }
    /**
     * Creates a tree with root labeled by terminal lbl and children chdn.
     * 
     * @param lbl  The label of the root
     * @param chdn Its children
     */
    public ParseTree(LexicalUnit lbl, List<ParseTree> chdn) {
        this.label = new Symbol(lbl);
        this.children = chdn;
    }
    /**
     * Creates a tree with root labeled by variable lbl and children chdn.
     * 
     * @param lbl  The label of the root
     * @param chdn Its children
     */
    public ParseTree(NonTerminal lbl, List<ParseTree> chdn) {
        this.label = new Symbol(null,lbl);
        this.children = chdn;
    }

    /* Pure LaTeX version (using the forest package) */
    /**
     * Writes the tree as LaTeX code.
     * 
     * @return the String representation of the tree as LaTeX code.
     */
    public String toLaTexTree() {
        StringBuilder treeTeX = new StringBuilder();
        treeTeX.append("[");
        treeTeX.append("{" + label.toTexString() + "}");   // Implement this yourself in Symbol.java
        treeTeX.append(" ");

        for (ParseTree child : children) {
            treeTeX.append(child.toLaTexTree());
        }
        treeTeX.append("]");
        return treeTeX.toString();
    }

    /**
     * Writes the tree as a forest picture. Returns the tree in forest enviroment using the LaTeX code of the tree.
     * 
     * @return the String representation of the tree as forest LaTeX code.
     */
    public String toForestPicture() {
        return "\\begin{forest}for tree={rectangle, draw, l sep=20pt}" + toLaTexTree() + ";\n\\end{forest}";
    }

    /**
     * Writes the tree as a LaTeX document which can be compiled using PDFLaTeX.
     * 
     * This method uses the forest package.
     * <br>
     * <br>
     * The result can be used with the command:
     * 
     * <pre>
     * pdflatex some-file.tex
     * </pre>
     * 
     * @return a String of a full LaTeX document (to be compiled with pdflatex)
     */
    public String toLaTeXusingForest() {
        return "\\documentclass[border=5pt]{standalone}\n\n\\usepackage{forest}\n\n\\begin{document}\n\n" +
                toForestPicture()
                + "\n\n\\end{document}\n%% Local Variables:\n%% TeX-engine: lualatex\n%% End:";
    }

    /* Tikz version (using graphs and graphdrawing libraries, with GD library trees, requiring LuaLaTeX) */
    /**
     * Writes the tree as TikZ code. TikZ is a language to specify drawings in LaTeX files.
     * 
     * @return the String representation of the tree as TikZ code.
     */
    public String toTikZ() {
        StringBuilder treeTikZ = new StringBuilder();
        treeTikZ.append("node {");
        treeTikZ.append(label.toTexString());  // Implement this yourself in Symbol.java
        treeTikZ.append("}\n");
        for (ParseTree child : children) {
            treeTikZ.append("child { ");
            treeTikZ.append(child.toTikZ());
            treeTikZ.append(" }\n");
        }
        return treeTikZ.toString();
    }

    /**
     * Writes the tree as a TikZ picture. A TikZ picture embeds TikZ code so that LaTeX undertands it.
     * 
     * @return the String representation of the tree as a TikZ picture.
     */
    public String toTikZPicture() {
        return "\\begin{tikzpicture}[tree layout,every node/.style={draw,rounded corners=3pt}]\n\\" + toTikZ() + ";\n\\end{tikzpicture}";
    }

    /**
     * Writes the tree as a LaTeX document which can be compiled using LuaLaTeX.
     * 
     * This method uses the Tikz package.
     * <br>
     * <br>
     * The result can be used with the command:
     * 
     * <pre>
     * lualatex some-file.tex
     * </pre>
     * 
     * @return a String of a full LaTeX document (to be compiled with lualatex)
     */
    public String toLaTeXusingTikz() {
        return "\\documentclass[border=5pt]{standalone}\n\n\\usepackage{tikz}\\usetikzlibrary{graphs,graphdrawing}\\usegdlibrary{trees}\n\n\\begin{document}\n\n" +
                toTikZPicture()
                + "\n\n\\end{document}\n%% Local Variables:\n%% TeX-engine: lualatex\n%% End:";
    }

    /* Alias */
    /**
     * Writes the tree as a LaTeX document which can be compiled using LuaLaTeX.
     * 
     * This is an alias of {@link toLaTeXusingForest() toLaTeXusingForest}.
     * <br>
     * <br>
     * The result can be used with the command:
     * 
     * <pre>
     * pdflatex some-file.tex
     * </pre>
     * 
     * @return a String of a full LaTeX document (to be compiled with pdflatex)
     */
    public String toLaTeX() {
        return this.toLaTeXusingForest();
    }
}
