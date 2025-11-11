
import java.io.IOException;

/**
 * Recursive descent parser for the YaLCC language.
 * Constructs a parse tree based on the provided lexical analyzer.
 * Each parsing method corresponds to a grammar production.
 */
public class Parser {

    /** The lexical analyzer providing tokens for parsing. */
    private final LexicalAnalyzer lexer;

    /** The current token being analyzed */
    private Symbol currentToken;

    /**
     * Creates a parser with the given lexical analyzer.
     * @param lexer the lexical analyzer
     * @throws Exception if an error occurs during token retrieval
     */
    public Parser(LexicalAnalyzer lexer) throws IOException {
        this.lexer = lexer;
        this.currentToken = lexer.yylex();
    }

    /**
     * Matches the current token against the expected token type.
     * Advances to the next token if matched; throws an error otherwise.
     * @param expected the expected token type
     * @throws ParseException if the current token does not match the expected type
     * @throws IOException if an I/O error occurs during token retrieval
     */
    private void match(LexicalUnit expected) throws ParseException, IOException {

        // if current token matches expected, advance to next token
        if (currentToken.getType() == expected) {

            currentToken = lexer.yylex();

        } else {

            // if current token does not match expected, throw an error

            throw new ParseException(
                "Syntax Error: Expected " + expected + " but found " + currentToken.getType(),
                currentToken.getLine(),
                currentToken.getColumn(),
                expected,
                currentToken.getType()
            );
        
        }
    }

    /**
     * Creates a dummy symbol for non-terminal nodes in the parse tree.
     * @param nt the non-terminal symbol
     * @return a Symbol representing the non-terminal
     */
    private Symbol dummy(NonTerminal nt) {
        return new Symbol(null, -1, -1, nt);
    }

    /**
     * Parse the entire program.
     * @return the parse tree node for Program
     * @throws ParseException if a syntax error occurs
     * @throws IOException if an I/O error occurs
     */
    public ParseTree parseProgram() throws ParseException, IOException {

        // create the parse tree node for Program
        // use the production: Program → Prog PROGNAME Is Code End [1]
        ParseTree root = new ParseTree(dummy(NonTerminal.PROGRAM));
        root.setRuleNumber(1);

        match(LexicalUnit.PROG);
        root.addChild(new ParseTree(currentToken));
        match(LexicalUnit.PROGNAME);
        match(LexicalUnit.IS);
        root.addChild(parseCode());
        match(LexicalUnit.END);

        return root;

    }

    /**
     * Parse code block.
     * @return the parse tree node for Code
     * @throws ParseException if a syntax error occurs
     * @throws IOException if an I/O error occurs
     */
    private ParseTree parseCode() throws ParseException, IOException {

        // create the parse tree node for Code
        // use the productions: Code → Instruction ; Code [2] if applicable
        // else Code → ε [3]
        ParseTree node = new ParseTree(dummy(NonTerminal.CODE));

        switch (currentToken.getType()) {

            case VARNAME, IF, WHILE, PRINT, INPUT -> {

                node.setRuleNumber(2);

                node.addChild(parseInstruction());
                match(LexicalUnit.SEMI);
                node.addChild(parseCode());

            }

            default -> {
                // epsilon production
                // ?? i decided to add an epsilon node for clarity in the parse tree
                node.setRuleNumber(3);
                node.addChild(new ParseTree(new Symbol(LexicalUnit.EPSILON)));
            }
        }

        return node;

    }

    /**
     * Parse instruction statement.
     * @return the parse tree node for Instruction
     * @throws ParseException if a syntax error occurs
     * @throws IOException if an I/O error occurs
     */
    private ParseTree parseInstruction() throws ParseException, IOException {

        // create the parse tree node for Instruction
        // use the appropriate production based on the current token
        ParseTree node = new ParseTree(dummy(NonTerminal.INSTRUCTION));

        switch (currentToken.getType()) {

            case VARNAME -> {
                // use the production Varname → Assign [4]
                node.setRuleNumber(4);
                node.addChild(parseAssign());
            }

            case IF -> {
                // use the production If → If [5]
                node.setRuleNumber(5);
                node.addChild(parseIf());
            }

            case WHILE -> {
                // use the production While → While [6]
                node.setRuleNumber(6);
                node.addChild(parseWhile());
            }

            case PRINT -> {
                // use the production Output → Output [7]
                node.setRuleNumber(7);
                node.addChild(parseOutput());
            }

            case INPUT -> {
                // use the production Input → Input [8]
                node.setRuleNumber(8);
                node.addChild(parseInput());
            }

            default -> throw new ParseException(
                "Syntax Error: Unexpected token " + currentToken.getType() + " in instruction",
                currentToken.getLine(),
                currentToken.getColumn(),
                null,
                currentToken.getType()
            );

        }

        return node;

    }

    /**
     * Parse assignment statement.
     * @return the parse tree node for Assign
     * @throws ParseException if a syntax error occurs
     * @throws IOException if an I/O error occurs
     */
    private ParseTree parseAssign() throws ParseException, IOException {

        // create the parse tree node for Assign
        // use the production: VARNAME → VarName = ExprArith [9]
        ParseTree node = new ParseTree(dummy(NonTerminal.ASSIGN));
        node.setRuleNumber(9);

        node.addChild(new ParseTree(currentToken));
        match(LexicalUnit.VARNAME);
        match(LexicalUnit.ASSIGN);
        node.addChild(parseExprArith());

        return node;

    }

    /**
     * Parse If statement.
     * @return the parse tree node for If
     * @throws ParseException if a syntax error occurs
     * @throws IOException if an I/O error occurs
     */
    private ParseTree parseIf() throws ParseException, IOException {

        // create the parse tree node for If
        ParseTree node = new ParseTree(dummy(NonTerminal.IF));

        match(LexicalUnit.IF);
        match(LexicalUnit.LBRACK);
        node.addChild(parseCond());
        match(LexicalUnit.RBRACK);
        match(LexicalUnit.THEN);
        node.addChild(parseCode());

        // check for optional else branch
        parseOptionalElse(node);

        match(LexicalUnit.END);
        return node;

    }

    /** 
     * Handles the optional Else branch in an If statement.
     * If the current token is ELSE, it parses the else branch.
     * @param node the If parse tree node to which the else branch will be added
     * @throws ParseException if a syntax error occurs
     * @throws IOException if an I/O error occurs
     */
    private void parseOptionalElse(ParseTree node) throws ParseException, IOException {

        if (currentToken.getType() == LexicalUnit.ELSE) {

            // use the production If → If { Cond } Then Code Else Code End [11]
            node.setRuleNumber(11);
            match(LexicalUnit.ELSE);
            node.addChild(parseCode());

        } else {

            // use the production If → If { Cond } Then Code End [10]
            node.setRuleNumber(10);

        }

    }

    /**
     * Parse While statement.
     * @return the parse tree node for While
     * @throws ParseException if a syntax error occurs
     * @throws IOException if an I/O error occurs
     */
    private ParseTree parseWhile() throws ParseException, IOException {

        // create the parse tree node for While
        // use the production: While → While { Cond } Do Code End [12]
        ParseTree node = new ParseTree(dummy(NonTerminal.WHILE));
        node.setRuleNumber(12);

        match(LexicalUnit.WHILE);
        match(LexicalUnit.LBRACK);
        node.addChild(parseCond());
        match(LexicalUnit.RBRACK);
        match(LexicalUnit.DO);
        node.addChild(parseCode());
        match(LexicalUnit.END);

        return node;

    }
    
    /**
     * Parse Output statement.
     * @return the parse tree node for Output
     * @throws ParseException if a syntax error occurs
     * @throws IOException if an I/O error occurs
     */
    private ParseTree parseOutput() throws ParseException, IOException {

        // create the parse tree node for Output
        // use the production: Output → Print ( VarName ) [13]
        ParseTree node = new ParseTree(dummy(NonTerminal.OUTPUT));
        node.setRuleNumber(13);

        match(LexicalUnit.PRINT);
        match(LexicalUnit.LPAREN);
        node.addChild(new ParseTree(currentToken));
        match(LexicalUnit.VARNAME);
        match(LexicalUnit.RPAREN);

        return node;

    }

    /**
     * Parse Input statement.
     * @return the parse tree node for Input
     * @throws ParseException if a syntax error occurs
     * @throws IOException if an I/O error occurs
     */
    private ParseTree parseInput() throws ParseException, IOException {

        // create the parse tree node for Input
        // use the production: Input → Input ( VarName ) [14]
        ParseTree node = new ParseTree(dummy(NonTerminal.INPUT));
        node.setRuleNumber(14);

        match(LexicalUnit.INPUT);
        match(LexicalUnit.LPAREN);
        node.addChild(new ParseTree(currentToken));
        match(LexicalUnit.VARNAME);
        match(LexicalUnit.RPAREN);

        return node;
    }

    /**
     * Parse condition.
     * @return the parse tree node for Cond
     * @throws ParseException if a syntax error occurs
     * @throws IOException if an I/O error occurs
     */
    private ParseTree parseCond() throws ParseException, IOException {

        // create the parse tree node for Cond
        // use the production: Cond → CondImpl [15]
        ParseTree node = parseCondImpl();
        node.setRuleNumber(15);

        return node;

    }

    /**
     * Parse condition with implication.
     * @return the parse tree node for CondImpl
     * @throws ParseException if a syntax error occurs
     * @throws IOException if an I/O error occurs
     */
    private ParseTree parseCondImpl() throws ParseException, IOException {

        // create the parse tree node for CondImpl
        // use the production: CondAtom -> CondImpl [16] / CondAtom [16']

        ParseTree condAtomNode = parseCondAtom();

        // check for implication
        if (currentToken.getType() == LexicalUnit.IMPLIES) {

            // this node is an implication
            ParseTree node = new ParseTree(dummy(NonTerminal.COND_IMPL));
            node.setRuleNumber(16);
            match(LexicalUnit.IMPLIES);

            node.addChild(condAtomNode);
            node.addChild(new ParseTree(new Symbol(LexicalUnit.IMPLIES)));
            node.addChild(parseCondImpl());

            return node;

        } else {

            // this node is just a CondAtom
            // use the production: CondAtom [16'], which is 16 in essence
            // ?? might want to make a separate rule number for clarity at some point

            condAtomNode.setRuleNumber(16);
            return condAtomNode;

        }

    }

    /**
     * Parse condition comparison.
     * @return the parse tree node for CondComp
     * @throws ParseException if a syntax error occurs
     * @throws IOException if an I/O error occurs
     */
    private ParseTree parseCondComp() throws ParseException, IOException {
        
        // create the parse tree node for CondComp
        // use the production: ExprArith Comp ExprArith [19]

        ParseTree left = parseExprArith();

        while (currentToken.getType() == LexicalUnit.EQUAL ||
               currentToken.getType() == LexicalUnit.SMALEQ ||
               currentToken.getType() == LexicalUnit.SMALLER) {

            // handle comparison operators
            LexicalUnit op = currentToken.getType();
            match(op);
            ParseTree right = parseExprArith();
        
            // make the comparison node
            ParseTree node = new ParseTree(dummy(NonTerminal.COND_COMP));
            node.setRuleNumber(19);

            node.addChild(left);
            node.addChild(new ParseTree(new Symbol(op)));
            node.addChild(right);

            left = node;  // left associativity

        }

        return left;

    }

    /**
     * Parse condition atom.
     * @return the parse tree node for CondAtom
     * @throws ParseException if a syntax error occurs
     * @throws IOException if an I/O error occurs
     */
    private ParseTree parseCondAtom() throws ParseException, IOException {

        // check for pipe
        if (currentToken.getType() == LexicalUnit.PIPE) {

            // use the production: | Cond | [17]
            ParseTree node = new ParseTree(dummy(NonTerminal.COND_ATOM));
            node.setRuleNumber(17);

            match(LexicalUnit.PIPE);
            node.addChild(parseCond());
            match(LexicalUnit.PIPE);

            return node;

        } else {

            // create the parse tree node for CondAtom
            // use the production: CondComp [18]
            ParseTree node = parseCondComp();
            node.setRuleNumber(18);
            return node;
            
        }

    }

    /**
     * Parse arithmetic expression.
     * @return the parse tree node for ExprArith
     * @throws ParseException if a syntax error occurs
     * @throws IOException if an I/O error occurs
     */
    private ParseTree parseExprArith() throws ParseException, IOException {

        // create the parse tree node for ExprArith
        // use the production: ExprAddSub [20]
        ParseTree node = parseExprAddSub();
        node.setRuleNumber(20);

        return node;

    }

    /**
     * Parse addition and subtraction.
     * @return the parse tree node for ExprAddSub
     * @throws ParseException if a syntax error occurs
     * @throws IOException if an I/O error occurs
     */
    private ParseTree parseExprAddSub() throws ParseException, IOException {

        // left recursion elimination
        // parent is the root for each addition/subtraction
        ParseTree node = parseExprMulDiv();

        // while there are + or - operators, keep building the tree
        while (currentToken.getType() == LexicalUnit.PLUS || currentToken.getType() == LexicalUnit.MINUS) {

            LexicalUnit op = currentToken.getType();
            match(op);

            ParseTree right = parseExprMulDiv();
            // create parent node
            // use production: ExprAddSub → ExprMulDiv { (+|-) ExprMulDiv }* [21]
            ParseTree parent = new ParseTree(dummy(NonTerminal.EXPR_ADDSUB));
            parent.setRuleNumber(op == LexicalUnit.PLUS ? 20 : 20);

            parent.addChild(node);
            parent.addChild(new ParseTree(new Symbol(op)));
            parent.addChild(right);

            node = parent;

        }

        return node;

    }

    /**
     * Parse multiplication and division.
     * @return the parse tree node for ExprMulDiv
     * @throws ParseException if a syntax error occurs
     * @throws IOException if an I/O error occurs
     */
    private ParseTree parseExprMulDiv() throws ParseException, IOException {

        // left recursion elimination
        // parent is the root for each multiplication/division
        ParseTree node = parseExprUnary();

        // while there are * or / operators, keep building the tree
        while (currentToken.getType() == LexicalUnit.TIMES || currentToken.getType() == LexicalUnit.DIVIDE) {

            LexicalUnit op = currentToken.getType();
            match(op);

            ParseTree right = parseExprUnary();
            // create parent node
            // use production: ExprMulDiv → ExprUnary { (*|/) ExprUnary }* [22]
            ParseTree parent = new ParseTree(dummy(NonTerminal.EXPR_MULDIV));
            parent.setRuleNumber(22);

            parent.addChild(node);
            parent.addChild(new ParseTree(new Symbol(op)));
            parent.addChild(right);

            node = parent;

        }

        return node;

    }

    /**
     * Parse unary expression.
     * @return the parse tree node for ExprUnary
     * @throws ParseException if a syntax error occurs
     * @throws IOException if an I/O error occurs
     */
    private ParseTree parseExprUnary() throws ParseException, IOException {

        // if there is a unary minus, handle it
        if (currentToken.getType() == LexicalUnit.MINUS) {

            match(LexicalUnit.MINUS);
            // create the parse tree node for unary minus
            // use the production: - ExprPrimary [23]
            ParseTree node = new ParseTree(dummy(NonTerminal.UNARY_MINUS));
            node.setRuleNumber(23);
            node.addChild(parseExprPrimary());  // add ExprPrimary tree
            return node;

        } else {

            // no unary minus, just parse ExprPrimary
            // use the production: ExprPrimary [24]
            ParseTree node = parseExprPrimary();
            node.setRuleNumber(24);
            return node;

        }

    }

    /**
     * Parse primary expression.
     * @return the parse tree node for ExprPrimary
     * @throws ParseException if a syntax error occurs
     * @throws IOException if an I/O error occurs
     */
    private ParseTree parseExprPrimary() throws ParseException, IOException {

        // create the parse tree node for ExprPrimary
        // use the appropriate production based on the current token
        ParseTree node;

        switch (currentToken.getType()) {
            case VARNAME -> { node = new ParseTree(currentToken); node.setRuleNumber(25); match(LexicalUnit.VARNAME); }
            case NUMBER  -> { node = new ParseTree(currentToken); node.setRuleNumber(26); match(LexicalUnit.NUMBER); }
            case LPAREN  -> { match(LexicalUnit.LPAREN); node = parseExprArith(); match(LexicalUnit.RPAREN); node.setRuleNumber(27); }
            default -> throw new ParseException(
                "Syntax Error: Unexpected token " + currentToken.getType() + " in expression",
                currentToken.getLine(),
                currentToken.getColumn(),
                null,
                currentToken.getType()
            );
        }

        return node;

    }

}
