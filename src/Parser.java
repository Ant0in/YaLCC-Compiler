

/**
 * Recursive descent parser for the YaLCC language.
 * Constructs a parse tree based on the provided lexical analyzer.
 * Each parsing method corresponds to a grammar production.
 */
public class Parser {

    /** Enumeration of non-terminal symbols in the YaLCC grammar. */
    public enum NonTerminal {
        PROGRAM,
        CODE,
        INSTRUCTION,
        ASSIGN,
        IF,
        WHILE,
        OUTPUT,
        INPUT,
        COND,
        COND_IMPL,
        COND_BASE,
        EXPR_ARITH,
        EXPR_ADDSUB,
        EXPR_MULDIV,
        EXPR_UNARY,
        EXPR_PRIMARY,
        UNARY_MINUS
    }

    /** The lexical analyzer providing tokens for parsing. */
    private final LexicalAnalyzer lexer;

    /** The current token being analyzed */
    private Symbol currentToken;

    /**
     * Creates a parser with the given lexical analyzer.
     * @param lexer the lexical analyzer
     * @throws Exception if an error occurs during token retrieval
     */
    public Parser(LexicalAnalyzer lexer) throws Exception {
        this.lexer = lexer;
        this.currentToken = lexer.yylex();
    }

    /**
     * Matches the current token against the expected token type.
     * Advances to the next token if matched; throws an error otherwise.
     * @param expected the expected token type
     * @throws Exception if the current token does not match the expected type
     */
    private void match(LexicalUnit expected) throws Exception {

        // if current token matches expected, advance to next token
        if (currentToken.getType() == expected) {

            currentToken = lexer.yylex();

        } else {

            // if current token does not match expected, throw an error
            // ?? we might want to make more precise error messages later, but for now this will do

            throw new Exception("Syntax error at line " + (currentToken.getLine()) + ", column " + (currentToken.getColumn()) + ": expected " + expected + ", got " + currentToken.getType());
        
        }
    }

    /**
     * Creates a dummy symbol for non-terminal nodes in the parse tree.
     * @param name the name of the non-terminal
     * @return a Symbol representing the non-terminal
     */
    private Symbol dummy(NonTerminal nt) {
        return new Symbol(null, -1, -1, nt.toString());
    }

    /**
     * Production rule for Program → Prog PROGNAME Is Code End
     * @return the parse tree node for Program
     * @throws Exception if a syntax error occurs
     */
    public ParseTree parseProgram() throws Exception {

        // create the parse tree node for Program
        ParseTree root = new ParseTree(dummy(NonTerminal.PROGRAM));

        // use the production Program → Prog PROGNAME Is Code End
        root.setRuleNumber(1);

        match(LexicalUnit.PROG);
        root.addChild(new ParseTree(currentToken));  // add PROGNAME node
        match(LexicalUnit.PROGNAME);
        match(LexicalUnit.IS);
        root.addChild(parseCode());  // add Code tree
        match(LexicalUnit.END);

        return root;

    }

    /**
     * Production rule for Code → Instruction ; Code | ε
     * @return the parse tree node for Code
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseCode() throws Exception {

        // create the parse tree node for Code
        ParseTree node = new ParseTree(dummy(NonTerminal.CODE));

        switch (currentToken.getType()) {

            // if the current token indicates the start of an instruction
            case VARNAME, IF, WHILE, PRINT, INPUT -> {

                // use the production Code → Instruction ; Code
                node.setRuleNumber(2);
                node.addChild(parseInstruction());
                // expect a semicolon after the instruction
                match(LexicalUnit.SEMI);
                node.addChild(parseCode());

            }

            default -> {
                // use the production Code → ε
                node.setRuleNumber(3);
            }
        }

        return node;
    }

    /**
     * Production rule for Instruction → Assign | If | While | Output | Input
     * @return the parse tree node for Instruction
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseInstruction() throws Exception {

        // create the parse tree node for Instruction
        ParseTree node = new ParseTree(dummy(NonTerminal.INSTRUCTION));

        // use the production Instruction → Assign | If | While | Output | Input
        switch (currentToken.getType()) {
            case VARNAME -> node.addChild(parseAssign());
            case IF -> node.addChild(parseIf());
            case WHILE -> node.addChild(parseWhile());
            case PRINT -> node.addChild(parseOutput());
            case INPUT -> node.addChild(parseInput());
            default -> throw new Exception("Unexpected token " + currentToken.getType());
        }

        return node;

    }

    /**
     * Production rule for Assign → VarName = ExprArith
     * @return the parse tree node for Assign
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseAssign() throws Exception {

        // create the parse tree node for Assign
        ParseTree node = new ParseTree(dummy(NonTerminal.ASSIGN));
        node.setRuleNumber(9);

        node.addChild(new ParseTree(currentToken));  // add VarName node
        match(LexicalUnit.VARNAME);
        match(LexicalUnit.ASSIGN);
        node.addChild(parseExprArith());  // add ExprArith tree

        return node;

    }

    /**
     * Production rule for If → If { Cond } Then Code End | If { Cond } Then Code Else Code End
     * @return the parse tree node for If
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseIf() throws Exception {

        // create the parse tree node for If
        ParseTree node = new ParseTree(dummy(NonTerminal.IF));

        match(LexicalUnit.IF);
        match(LexicalUnit.LBRACK);
        node.addChild(parseCond());  // add Cond tree
        match(LexicalUnit.RBRACK);
        match(LexicalUnit.THEN);
        node.addChild(parseCode());  // add Code tree (if-then branch)

        // check for optional else branch
        parseOptionalElse(node);

        match(LexicalUnit.END);
        return node;

    }

    /** 
     * Handles the optional Else branch in an If statement.
     * If the current token is ELSE, it parses the else branch.
     * @param node the If parse tree node to which the else branch will be added
     * @throws Exception if a syntax error occurs
     */
    private void parseOptionalElse(ParseTree node) throws Exception {

        if (currentToken.getType() == LexicalUnit.ELSE) {

            // use the production If → If { Cond } Then Code Else Code End
            node.setRuleNumber(11);
            match(LexicalUnit.ELSE);
            node.addChild(parseCode());  // add Code tree (else branch)

        } else {

            // use the production If → If { Cond } Then Code End
            node.setRuleNumber(10);

        }

    }


    /**
     * Production rule for While → While { Cond } Do Code End
     * @return the parse tree node for While
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseWhile() throws Exception {

        // create the parse tree node for While
        ParseTree node = new ParseTree(dummy(NonTerminal.WHILE));
        node.setRuleNumber(12);

        match(LexicalUnit.WHILE);
        match(LexicalUnit.LBRACK);
        node.addChild(parseCond());  // add Cond tree
        match(LexicalUnit.RBRACK);
        match(LexicalUnit.DO);
        node.addChild(parseCode());  // add Code tree
        match(LexicalUnit.END);

        return node;

    }

    /**
     * Production rule for Output → Print ( VarName )
     * @return the parse tree node for Output
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseOutput() throws Exception {

        // create the parse tree node for Output
        ParseTree node = new ParseTree(dummy(NonTerminal.OUTPUT));
        node.setRuleNumber(13);

        match(LexicalUnit.PRINT);
        match(LexicalUnit.LPAREN);
        node.addChild(new ParseTree(currentToken));  // add VarName node (leaf)
        match(LexicalUnit.VARNAME);
        match(LexicalUnit.RPAREN);

        return node;

    }

    /**
     * Production rule for Input → Input ( VarName )
     * @return the parse tree node for Input
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseInput() throws Exception {

        // create the parse tree node for Input
        ParseTree node = new ParseTree(dummy(NonTerminal.INPUT));
        node.setRuleNumber(14);

        match(LexicalUnit.INPUT);
        match(LexicalUnit.LPAREN);
        node.addChild(new ParseTree(currentToken));  // add VarName node (leaf)
        match(LexicalUnit.VARNAME);
        match(LexicalUnit.RPAREN);

        return node;
    }

    /**
     * Production rule for Cond → CondImpl
     * @return the parse tree node for Cond
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseCond() throws Exception {
        return parseCondImpl();
    }

    /**
     * Production rule for CondImpl → CondBase [-> CondImpl]?
     * @return the parse tree node for CondImpl
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseCondImpl() throws Exception {

        // create the parse tree node for CondImpl
        // left is the CondBase, right is the optional CondImpl
        // this uses left recursion elimination
        ParseTree left = parseCondBase();
        ParseTree node = new ParseTree(dummy(NonTerminal.COND_IMPL));

        // check for implication
        if (currentToken.getType() == LexicalUnit.IMPLIES) {

            node.setRuleNumber(16);
            match(LexicalUnit.IMPLIES);
            ParseTree right = parseCondImpl();  // recursive call for right side
            node.addChild(left);  // add left CondBase
            node.addChild(new ParseTree(new Symbol(LexicalUnit.IMPLIES)));
            node.addChild(right);  // add right CondImpl

        // no implication, just return the left CondBase
        } else {
            node.setRuleNumber(16);
            node.addChild(left);  // add left CondBase
        }

        return node;

    }

    /**
     * Production rule for CondBase → | Cond | ExprArith Comp ExprArith
     * @return the parse tree node for CondBase
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseCondBase() throws Exception {

        // create the parse tree node for CondBase
        ParseTree node = new ParseTree(dummy(NonTerminal.COND_BASE));

        // check for pipe
        if (currentToken.getType() == LexicalUnit.PIPE) {
            node.setRuleNumber(17);
            match(LexicalUnit.PIPE);
            node.addChild(parseCond());  // add Cond tree
            match(LexicalUnit.PIPE);

        } else {

            // if condition is not pipe, it must be ExprArith Comp ExprArith
            node.setRuleNumber(18);
            node.addChild(parseExprArith());  // add first ExprArith tree

            // match comparison operator
            switch (currentToken.getType()) {
                case EQUAL -> match(LexicalUnit.EQUAL);
                case SMALEQ -> match(LexicalUnit.SMALEQ);
                case SMALLER -> match(LexicalUnit.SMALLER);
                default -> throw new Exception("Expected comparison operator, got " + currentToken.getType());
            }

            node.addChild(parseExprArith());  // add second ExprArith tree
        }

        return node;

    }

    /**
     * Production rule for ExprArith → ExprAddSub
     * @return the parse tree node for ExprArith
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseExprArith() throws Exception {
        return parseExprAddSub();
    }

    /**
     * Production rule for ExprAddSub → ExprMulDiv { (+|-) ExprMulDiv }*
     * @return the parse tree node for ExprAddSub
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseExprAddSub() throws Exception {

        // left recursion elimination
        // parent is the root for each addition/subtraction
        ParseTree node = parseExprMulDiv();
        ParseTree parent;

        // while there are + or - operators, keep building the tree
        while (currentToken.getType() == LexicalUnit.PLUS || currentToken.getType() == LexicalUnit.MINUS) {

            LexicalUnit op = currentToken.getType();
            match(op);

            // parse the next ExprMulDiv (right operand)
            ParseTree right = parseExprMulDiv();

            // create a new parent node for the addition/subtraction
            parent = new ParseTree(dummy(NonTerminal.EXPR_ADDSUB));
            // ?? in the future, might be interesting to add proper rule numbers for + and -
            parent.setRuleNumber(op == LexicalUnit.PLUS ? 20 : 20);
            parent.addChild(node);
            parent.addChild(new ParseTree(new Symbol(op)));
            parent.addChild(right);
            node = parent;

        }

        return node;

    }

    /**
     * Production rule for ExprMulDiv → ExprUnary { (*|/) ExprUnary }*
     * @return the parse tree node for ExprMulDiv
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseExprMulDiv() throws Exception {

        // left recursion elimination
        // parent is the root for each multiplication/division
        ParseTree node = parseExprUnary();
        ParseTree parent;

        // while there are * or / operators, keep building the tree
        while (currentToken.getType() == LexicalUnit.TIMES || currentToken.getType() == LexicalUnit.DIVIDE) {

            LexicalUnit op = currentToken.getType();
            match(op);
            ParseTree right = parseExprUnary();
            parent = new ParseTree(dummy(NonTerminal.EXPR_MULDIV));

            // ?? in the future, might be interesting to add proper rule numbers for * and /
            parent.setRuleNumber(op == LexicalUnit.TIMES ? 21 : 21);
            parent.addChild(node);
            parent.addChild(new ParseTree(new Symbol(op)));
            parent.addChild(right);
            node = parent;

        }

        return node;

    }

    /**
     * Production rule for ExprUnary → - ExprPrimary | ExprPrimary
     * @return the parse tree node for ExprUnary
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseExprUnary() throws Exception {

        // if there is a unary minus, handle it
        if (currentToken.getType() == LexicalUnit.MINUS) {

            match(LexicalUnit.MINUS);
            ParseTree node = new ParseTree(dummy(NonTerminal.UNARY_MINUS));
            node.setRuleNumber(22);
            node.addChild(parseExprPrimary());  // add ExprPrimary tree
            return node;

        } else {

            // no unary minus, just parse ExprPrimary
            ParseTree node = parseExprPrimary();
            node.setRuleNumber(23);
            return node;

        }

    }

    /**
     * Production rule for ExprPrimary → VarName | Number | ( ExprArith )
     * @return the parse tree node for ExprPrimary
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseExprPrimary() throws Exception {

        // create the parse tree node for ExprPrimary
        ParseTree node;

        switch (currentToken.getType()) {
            case VARNAME -> {
                // production ExprPrimary → VarName
                node = new ParseTree(currentToken);
                node.setRuleNumber(24);
                match(LexicalUnit.VARNAME);
            }
            case NUMBER -> {
                // production ExprPrimary → Number
                node = new ParseTree(currentToken);
                node.setRuleNumber(25);
                match(LexicalUnit.NUMBER);
            }
            case LPAREN -> {
                // production ExprPrimary → ( ExprArith )
                match(LexicalUnit.LPAREN);
                node = parseExprArith();
                match(LexicalUnit.RPAREN);
                node.setRuleNumber(26);
            }

            // unexpected token default case
            default -> throw new Exception("Unexpected token in expression: " + currentToken.getType());
        }

        return node;

    }

}
