

/**
 * Recursive descent parser for the YaLCC language.
 * Constructs a parse tree based on the provided lexical analyzer.
 * Each parsing method corresponds to a grammar production.
 */
public class Parser {

    /** Enumeration of non-terminal symbols in the YaLCC grammar. */
    public enum NonTerminal {
        PROGRAM, CODE, INSTRUCTION, ASSIGN, IF, WHILE, OUTPUT, INPUT,
        COND_IMPL, COND_COMP, COND_ATOM, EXPR_ARITH, EXPR_ADDSUB, EXPR_MULDIV,
        EXPR_UNARY, EXPR_PRIMARY, UNARY_MINUS
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
     * Parse the entire program.
     * @return the parse tree node for Program
     * @throws Exception if a syntax error occurs
     */
    public ParseTree parseProgram() throws Exception {

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
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseCode() throws Exception {

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

            default -> node.setRuleNumber(3);
        }

        return node;

    }

    /**
     * Parse instruction statement.
     * @return the parse tree node for Instruction
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseInstruction() throws Exception {

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

            default -> throw new Exception("Unexpected token " + currentToken.getType());

        }

        return node;

    }

    /**
     * Parse assignment statement.
     * @return the parse tree node for Assign
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseAssign() throws Exception {

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
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseIf() throws Exception {

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
     * @throws Exception if a syntax error occurs
     */
    private void parseOptionalElse(ParseTree node) throws Exception {

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
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseWhile() throws Exception {

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
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseOutput() throws Exception {

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
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseInput() throws Exception {

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
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseCond() throws Exception {

        // create the parse tree node for Cond
        // use the production: Cond → CondImpl [15]
        ParseTree node = parseCondImpl();
        node.setRuleNumber(15);

        return node;

    }

    /**
     * Parse condition with implication.
     * @return the parse tree node for CondImpl
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseCondImpl() throws Exception {

        // create the parse tree node for CondImpl
        // use the production: CondAtom -> CondImpl [16] / CondAtom [16']
        ParseTree left = parseCondComp();

        // check for implication
        if (currentToken.getType() == LexicalUnit.IMPLIES) {

            // this node is an implication
            ParseTree node = new ParseTree(dummy(NonTerminal.COND_IMPL));
            match(LexicalUnit.IMPLIES);
            node.setRuleNumber(16);

            // right will be handled recursively
            ParseTree right = parseCond();

            // we have a: cond -> cond
            node.addChild(left);
            node.addChild(new ParseTree(new Symbol(LexicalUnit.IMPLIES)));
            node.addChild(right);

            return node;

        }

        // no implication, just return a condComp
        return left;

    }

    /**
     * Production rule for CondComp → CondBase ( == | <= | < ) CondBase
     * @return the parse tree node for CondComp
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseCondComp() throws Exception {
        
        ParseTree left = parseCondAtom();

        while (currentToken.getType() == LexicalUnit.EQUAL ||
               currentToken.getType() == LexicalUnit.SMALEQ ||
               currentToken.getType() == LexicalUnit.SMALLER) {

            LexicalUnit op = currentToken.getType();
            match(op);
            ParseTree right = parseCondAtom();

            // create a new node for the comparison
            ParseTree node = new ParseTree(dummy(NonTerminal.COND_COMP));
            node.setRuleNumber(18);
            node.addChild(left);
            node.addChild(new ParseTree(new Symbol(op)));
            node.addChild(right);

            left = node;  // left associativity

        }

        return left;

    }

    /**
     * Production rule for CondAtom → | CondImpl | | ExprArith
     * @return the parse tree node for CondAtom
     * @throws Exception if a syntax error occurs
     */
    private ParseTree parseCondAtom() throws Exception {

        // check for pipe
        if (currentToken.getType() == LexicalUnit.PIPE) {

            // use the production CondBase → | Cond |
            ParseTree node = new ParseTree(dummy(NonTerminal.COND_ATOM));
            match(LexicalUnit.PIPE);
            node.setRuleNumber(17);
            node.addChild(parseCond());
            match(LexicalUnit.PIPE);
            return node;

        } else {

            return parseExprArith();  // parse ExprArith Comp ExprArith
            
        }

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
