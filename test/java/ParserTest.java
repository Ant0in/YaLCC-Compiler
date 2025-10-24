
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.io.StringReader;


/**
 * Unit tests for the Parser class.
 */
public class ParserTest {

    /**
     * Helper method to create a Parser from an input string.
     * @param input the input string to parse
     * @return a Parser instance
     */
    private Parser makeParser(String input) throws Exception {
        
        // create a parser from input string
        LexicalAnalyzer lexer = new LexicalAnalyzer(new StringReader(input));
        return new Parser(lexer);

    }

    /**
     * Helper methods to match parse tree nodes.
     * @param node the parse tree node
     * @param expected the expected non-terminal
     * @return true if matches, false otherwise
     */
    private boolean matchNonTerminal(ParseTree node, Parser.NonTerminal expected) {
        Symbol label = node.getLabel();
        assertNotNull(label);
        return label.getType() == null && label.getValue().equals(expected.toString());
    }

    /**
     * Helper method to match terminal nodes.
     * @param node the parse tree node
     * @param expected the expected terminal value
     * @return true if matches, false otherwise
     */
    private boolean matchTerminal(ParseTree node, Object expected) {
        
        Symbol label = node.getLabel();
        assertNotNull(label);

        if (label.getType() == null) return false;

        // if expected is a LexicalUnit, compare types
        if (expected instanceof LexicalUnit) {
            return label.getType() == expected;
        }

        // otherwise compare values
        Object value = label.getValue();
        return value != null && value.equals(expected);

    }

    /**
     * Helper method to get a child node at a specific index.
     * @param node the parent parse tree node
     * @param index the index of the child
     * @return the child parse tree node
     */
    private ParseTree child(ParseTree node, int index) {
        assertTrue(index < node.getChildren().size(), "Node has fewer children than expected");
        return node.getChildren().get(index);
    }

    /**
     * Helper method to count children of a node.
     * @param node the parse tree node
     * @param expectedCount the expected number of children
     * @return true if the count matches, false otherwise
     */
    private boolean countChildren(ParseTree node, int expectedCount) {
        return node.getChildren().size() == expectedCount;
    }

    /**
     * Helper method to check if a node is a leaf (has no children).
     * @param node the parse tree node
     * @return true if the node is a leaf, false otherwise
     */
    private boolean isLeaf(ParseTree node) {
        return countChildren(node, 0);
    }

    /**
     * Assertion helper methods for better error messages.
     */
    private void assertNonTerminal(ParseTree node, Parser.NonTerminal expected) {
        assertTrue(matchNonTerminal(node, expected),
        "Expected non-terminal does not match (" + node.getLabel().getValue() + " != " + expected + ")");
    }

    /**
     * Assertion helper methods for better error messages.
     */
    private void assertTerminal(ParseTree node, Object expected) {
        assertTrue(matchTerminal(node, expected),
        "Expected terminal does not match (" + node.getLabel().getValue() + " != " + expected + ")");
        
    }

    /**
     * Assertion helper methods for better error messages.
     */
    private void assertChildCount(ParseTree node, int expectedCount) {
        assertTrue(countChildren(node, expectedCount),
        "Expected child count does not match (expected " + expectedCount + ", got " + node.getChildren().size() + ")");
    }

    /**
     * Assertion helper methods for better error messages.
     */
    private void assertIsLeaf(ParseTree node) {
        assertTrue(isLeaf(node), "Expected node to be a leaf, but it has children");
    }

    /**
     * Test parsing a minimal valid program.
     */
    @Test
    void testParseMinimalProgram() throws Exception {

        // minimal program
        String input = "Prog TEST Is End";
        Parser parser = makeParser(input);
        ParseTree tree = parser.parseProgram();

        // root is program
        assertNonTerminal(tree, Parser.NonTerminal.PROGRAM);
        assertChildCount(tree, 2);

        // PROGNAME
        ParseTree progNode = child(tree, 0);
        assertTerminal(progNode, "TEST");
        assertIsLeaf(progNode);

        // CODE (empty)
        ParseTree codeNode = child(tree, 1);
        assertNonTerminal(codeNode, Parser.NonTerminal.CODE);
        assertIsLeaf(codeNode);

    }

    /**
     * Test parsing a program with one statement.
     */
    @Test
    void testParseOneStatement() throws Exception {
        
        // program with one statement
        String input = "Prog MYPROG Is Input(a); End";
        Parser parser = makeParser(input);
        ParseTree tree = parser.parseProgram();

        // instruction node
        ParseTree instructionNode = child(child(tree, 1), 0); // CODE -> INSTRUCTION
        assertNonTerminal(instructionNode, Parser.NonTerminal.INSTRUCTION);
        assertChildCount(instructionNode, 1);

        // input statement
        ParseTree inputNode = child(instructionNode, 0);
        assertNonTerminal(inputNode, Parser.NonTerminal.INPUT);
        assertChildCount(inputNode, 1);

        // input variable (leaf)
        ParseTree varNode = child(inputNode, 0);
        assertTerminal(varNode, "a");
        assertIsLeaf(varNode);

    }

    /**
     * Test parsing a program with an assignment statement.
     */
    @Test
    void testParseAssignment() throws Exception {
        
        // program with assignment
        String input = "Prog ASSIGN Is a = 5; End";
        Parser parser = makeParser(input);
        ParseTree tree = parser.parseProgram();

        // instruction node
        ParseTree instructionNode = child(child(tree, 1), 0); // CODE -> INSTRUCTION
        assertNonTerminal(instructionNode, Parser.NonTerminal.INSTRUCTION);
        assertChildCount(instructionNode, 1);

        // assignment statement
        ParseTree assignNode = child(instructionNode, 0);
        assertNonTerminal(assignNode, Parser.NonTerminal.ASSIGN);
        assertChildCount(assignNode, 2);

        // variable
        ParseTree varNode = child(assignNode, 0);
        assertTerminal(varNode, "a");
        assertIsLeaf(varNode);

        // number
        ParseTree numNode = child(assignNode, 1);
        assertTerminal(numNode, 5);
        assertIsLeaf(numNode);

    }

    /**
     * Test parsing a program with a while loop.
     */
    @Test
    void testParseWhileLoop() throws Exception {
        
        // program with while loop
        String input = "Prog LOOP Is While { a < 10 } Do End; End";
        Parser parser = makeParser(input);
        ParseTree tree = parser.parseProgram();

        ParseTree whileNode = child(child(child(tree, 1), 0), 0); // CODE -> INSTRUCTION -> WHILE
        assertNonTerminal(whileNode, Parser.NonTerminal.WHILE);
        assertChildCount(whileNode, 2);

        // condimpl node
        ParseTree condImplNode = child(whileNode, 0);
        assertNonTerminal(condImplNode, Parser.NonTerminal.COND_IMPL);
        assertChildCount(condImplNode, 1);

        // condition node
        ParseTree condNode = child(condImplNode, 0);
        assertNonTerminal(condNode, Parser.NonTerminal.COND_BASE);
        assertChildCount(condNode, 2);

        // condition left side
        ParseTree leftNode = child(condNode, 0);
        assertTerminal(leftNode, "a");
        assertIsLeaf(leftNode);

        // condition right side
        ParseTree rightNode = child(condNode, 1);
        assertTerminal(rightNode, 10);
        assertIsLeaf(rightNode);

        // code inside while node
        ParseTree codeNode = child(whileNode, 1);
        assertNonTerminal(codeNode, Parser.NonTerminal.CODE);
        assertIsLeaf(codeNode);

    }

    /**
     * Test parsing a program with an expression in assignment.
     */
    @Test
    void testParseExpression() throws Exception {
        
        // program with expression
        String input = "Prog EXPR Is a = (b + 3) * 2; End";
        Parser parser = makeParser(input);
        ParseTree tree = parser.parseProgram();

        ParseTree assignNode = child(child(child(tree, 1), 0), 0); // CODE -> INSTRUCTION -> ASSIGN
        assertNonTerminal(assignNode, Parser.NonTerminal.ASSIGN);
        assertChildCount(assignNode, 2);

        // left side (variable)
        ParseTree varNode = child(assignNode, 0);
        assertTerminal(varNode, "a");
        assertIsLeaf(varNode);

        // right side (expression)
        ParseTree exprNode = child(assignNode, 1);
        assertNonTerminal(exprNode, Parser.NonTerminal.EXPR_MULDIV);
        assertChildCount(exprNode, 3);

        // 2nd child should be TIMES
        ParseTree timesNode = child(exprNode, 1);
        assertTerminal(timesNode, LexicalUnit.TIMES);
        assertIsLeaf(timesNode);

        // 3rd child should be 2
        ParseTree numNode = child(exprNode, 2);
        assertTerminal(numNode, 2);
        assertIsLeaf(numNode);

        // 1st child should be EXPR_ADDSUB
        ParseTree addSubNode = child(exprNode, 0);
        assertNonTerminal(addSubNode, Parser.NonTerminal.EXPR_ADDSUB);
        assertChildCount(addSubNode, 3);

        // 1st child of addSubNode should be variable b
        ParseTree bNode = child(addSubNode, 0);
        assertTerminal(bNode, "b");
        assertIsLeaf(bNode);

        // 2nd child of addSubNode should be PLUS
        ParseTree plusNode = child(addSubNode, 1);
        assertTerminal(plusNode, LexicalUnit.PLUS);
        assertIsLeaf(plusNode);

        // 3rd child of addSubNode should be number 3
        ParseTree threeNode = child(addSubNode, 2);
        assertTerminal(threeNode, 3);
        assertIsLeaf(threeNode);
    }

    /**
     * Test parsing a program with an if-else statement.
     */
    @Test
    void testParseIfElse() throws Exception {
        
        // program with if-else statement
        String input = "Prog IFELSE Is If { x == 0 } Then Else End; End";
        Parser parser = makeParser(input);
        ParseTree tree = parser.parseProgram();

        ParseTree ifNode = child(child(child(tree, 1), 0), 0); // CODE -> INSTRUCTION -> IF
        assertNonTerminal(ifNode, Parser.NonTerminal.IF);
        assertChildCount(ifNode, 3);

        // condition node
        ParseTree condImplNode = child(ifNode, 0);
        assertNonTerminal(condImplNode, Parser.NonTerminal.COND_IMPL);
        assertChildCount(condImplNode, 1);

        // then code node
        ParseTree thenCodeNode = child(ifNode, 1);
        assertNonTerminal(thenCodeNode, Parser.NonTerminal.CODE);
        assertIsLeaf(thenCodeNode);

        // else code node
        ParseTree elseCodeNode = child(ifNode, 2);
        assertNonTerminal(elseCodeNode, Parser.NonTerminal.CODE);
        assertIsLeaf(elseCodeNode);

    }

}