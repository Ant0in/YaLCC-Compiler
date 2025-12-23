import java.util.List;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

/**
 * Parser for Yalcc.
 * 
 * The parser implements a recursive descent mimicking the run of the pushdown automaton: the call stack replacing the automaton stack.
 * 
 * @author Mrudula Balachander, inspired from earlier versions of the project (exact authors not determined).
 *
 */
public class Parser{
    /**
     * Lexer object for the parsed file.
     */
    private LexicalAnalyzer scanner;
    /**
     * Current symbol at the head of the word to be read. This corresponds to the look-ahead (of length 1).
     */
    private Symbol current;
    /**
     * Option to print only the rule number (false) or the full rule (true).
     */
    private boolean fullRuleDisplay=false;
    /**
     * Width (in characters) of the widest left handside in a production rule.
     */
    private static final int widestNonTerm=13; // <Instruction>
    /**
     * Width (in characters) of the highest rule number.
     */
    private static final int log10ruleCard=2; // 35 rules

    /**
     * Creates a Parser object for the provided file and initialized the look-ahead.
     * 
     * @param source a FileReader object for the parsed file.
     * @throws IOException in case the lexing fails (syntax error).
     */
    public Parser(FileReader source) throws IOException{
        this.scanner = new LexicalAnalyzer(source);
        this.current = scanner.nextToken();
    }
    
    /* Display of the rules */
    /**
     * Returns a string of several spaces.
     * 
     * @param n the number of spaces.
     * @return a String containing n spaces.
     */
    private static String multispace(int n) {
        String res="";
        for (int i=0;i<n;i++) {
            res+=" ";
        };
        return res;
    }
    
    /**
     * Outputs the rule used in the LL descent.
     * 
     * @param rNum the rule number.
     * @param ruleLhs the left hand-side of the rule as a String.
     * @param ruleRhs the right hand-side of the rule as a String.
     * @param full a boolean specifying whether to write only the rule number (false) or the full rule (true).
     */
    private static void ruleOutput(int rNum, String ruleLhs,String ruleRhs, boolean full) {
        if (full) {
            System.out.println("   ["+rNum+"]"+
                multispace(1+log10ruleCard-String.valueOf(rNum).length())+ // Align left hand-sides regardless of number of digits in rule number
                ruleLhs+multispace(2+widestNonTerm-ruleLhs.length())+ // Align right hand-sides regardless of length of the left hand-side
                "→  "+ruleRhs);
        } else {
            System.out.print(rNum+" ");
        }
    }
    
    /**
     * Outputs the rule used in the LL descent, using the fullRuleDisplay value to set the option of full display or not.
     * 
     * @param rNum the rule number.
     * @param ruleLhs the left hand-side of the rule as a String.
     * @param ruleRhs the right hand-side of the rule as a String.
     */
    private void ruleOutput(int rNum, String ruleLhs,String ruleRhs) {
        ruleOutput(rNum,ruleLhs,ruleRhs,this.fullRuleDisplay);
    }
    
    /**
     * Sets the display option to "Full rules".
     */
    public void displayFullRules() {
        this.fullRuleDisplay=true;
    }
    
    /**
     * Sets the display option to "Rule numbers only".
     */
    public void displayRuleNumbers() {
        this.fullRuleDisplay=false;
    }

    /* Matching of terminals */
    /**
     * Advances in the input stream, consuming one token.
     * 
     * @throws IOException in case the lexing fails (syntax error).
     */
    private void consume() throws IOException{
        current = scanner.nextToken();
    }

    /**
     * Matches a (terminal) token from the head of the word.
     * 
     * @param token then LexicalUnit (terminal) to be matched.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the matching fails (syntax error): the next tolen is not the one to be matched.
     * @return a ParseTree made of a single leaf (the matched terminal).
     */
    private ParseTree match(LexicalUnit token) throws IOException, ParseException{
        if(!current.getType().equals(token)){
            // There is a parsing error
            throw new ParseException(current, Arrays.asList(token));
        }
        else {
            Symbol cur = current;
            consume();
            return new ParseTree(cur);
        }
    }
    
    /* Applying grammar rules */
    /**
     * Parses the file.
     * 
     * @return a ParseTree containing the parsed file structured by the grammar rules.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    public ParseTree parse() throws IOException, ParseException{
        // Program is the initial symbol of the grammar
        ParseTree pt = program();
        if (!this.fullRuleDisplay) {System.out.println();} // New line at the end of list of rules
        return pt;
    }
    
    /**
     * Treats a &lt;Program&gt; at the top of the stack.
     * 
     * Tries to apply rule [1]&nbsp;&lt;Prog&gt;&nbsp;&rarr;&nbsp;<code>Prog [ProgName] Is</code> &lt;Code&gt; <code>End</code>
     * 
     * @return a ParseTree with a &lt;Program&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree program() throws IOException, ParseException{
        // [1] <Program>  ->  begin <Code> end
        ruleOutput(1,"<Program>","Prog [ProgName] Is <Code> End");
        return new ParseTree(NonTerminal.Program, Arrays.asList(
            match(LexicalUnit.PROG),
            match(LexicalUnit.PROGNAME),
            match(LexicalUnit.IS),
            code(),
            match(LexicalUnit.END)
        ));
    }
    
    /**
     * Treats a &lt;Code&gt; at the top of the stack.
     * 
     * Tries to apply one of the rules <ul>
     *   <li>[2]&nbsp;&lt;Code&gt;&nbsp;&rarr;&nbsp;&lt;Instruction&gt;&nbsp;<code>;</code>&nbsp;&lt;Code&gt;</li>
     *   <li>[3]&nbsp;&lt;Code&gt;&nbsp;&rarr;&nbsp;&epsilon;</li>
     * </ul>
     * 
     * @return a ParseTree with a &lt;Code&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree code() throws IOException, ParseException{
        switch(current.getType()) {
            // [2] <Code>  ->  <Instruction>;<Code>
            case IF:
            case WHILE:
            case PRINT:
            case INPUT:
            case VARNAME:
                ruleOutput(2,"<Code>","<Instruction>;<Code>");
                return new ParseTree(NonTerminal.Code, Arrays.asList(
                    instruction(),
                    match(LexicalUnit.SEMI),
                    code()
                ));
            // [3] <Code>  ->  EPSILON 
            case END:
            case ELSE:
                ruleOutput(3,"<Code>","ɛ");
                return new ParseTree(NonTerminal.Code, Arrays.asList(
                    new ParseTree(LexicalUnit.EPSILON)
                ));
            default:
                throw new ParseException(current,NonTerminal.Code,Arrays.asList(
                    LexicalUnit.IF,
                    LexicalUnit.ELSE,
                    LexicalUnit.WHILE,
                    LexicalUnit.PRINT,
                    LexicalUnit.INPUT,
                    LexicalUnit.VARNAME,
                    LexicalUnit.END
                ));
        }
    }

    /**
     * Treats a &lt;Instruction&gt; at the top of the stack.
     * 
     * Tries to apply one of the rules <ul>
     *   <li>[4]&nbsp;&lt;Instruction&gt;&nbsp;&rarr;&nbsp;&lt;Assign&gt;</li>
     *   <li>[5]&nbsp;&lt;Instruction&gt;&nbsp;&rarr;&nbsp;&lt;If&gt;</li>
     *   <li>[6]&nbsp;&lt;Instruction&gt;&nbsp;&rarr;&nbsp;&lt;While&gt;</li>
     *   <li>[7]&nbsp;&lt;Instruction&gt;&nbsp;&rarr;&nbsp;&lt;Output&gt;</li>
     *   <li>[8]&nbsp;&lt;Instruction&gt;&nbsp;&rarr;&nbsp;&lt;Input&gt;</li>
     * </ul>
     * 
     * @return a ParseTree with a &lt;Instruction&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree instruction() throws IOException, ParseException{
        switch(current.getType()) {
            // [4] <Instruction>  ->  <Assign>
            case VARNAME:
                ruleOutput(4,"<Instruction>","<Assign>");
                return new ParseTree(NonTerminal.Instruction, Arrays.asList(
                    assignExpr()
                ));
            // [5] <Instruction>  ->  <If>
            case IF:
                ruleOutput(5,"<Instruction>","<If>");
                return new ParseTree(NonTerminal.Instruction, Arrays.asList(
                    ifExpr()
                ));
            // [6] <Instruction>  ->  <While>
            case WHILE:
                ruleOutput(6,"<Instruction>","<While>");
                return new ParseTree(NonTerminal.Instruction, Arrays.asList(
                    whileExpr()
                ));
            // [7] <Instruction>  ->  <Output>
            case PRINT:
                ruleOutput(7,"<Instruction>","<Output>");
                return new ParseTree(NonTerminal.Instruction, Arrays.asList(
                    outputExpr()
                ));
            // [8] <Instruction>  ->  <Input>
            case INPUT:
                ruleOutput(8,"<Instruction>","<Input>");
                return new ParseTree(NonTerminal.Instruction, Arrays.asList(
                    inputExpr()
                ));
            default:
                throw new ParseException(current,NonTerminal.Instruction,Arrays.asList(
                    LexicalUnit.VARNAME,
                    LexicalUnit.IF,
                    LexicalUnit.WHILE,
                    LexicalUnit.PRINT,
                    LexicalUnit.INPUT
                ));
        }
    }
    
    /**
     * Treats a &lt;Assign&gt; at the top of the stack.
     * 
     * Tries to apply rule [9]&nbsp;&lt;Assign&gt;&nbsp;&rarr;&nbsp;[Varname]<code>:=</code>&lt;ExprArith&gt;
     * 
     * @return a ParseTree with a &lt;Assign&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree assignExpr() throws IOException, ParseException{
        // [9] <Assign>  ->  [Varname] = <ExprArith>
        ruleOutput(9,"<Assign>","[Varname] = <ExprArith>");
        return new ParseTree(NonTerminal.Assign, Arrays.asList(
            match(LexicalUnit.VARNAME),
            match(LexicalUnit.ASSIGN),
            exprArith()
        ));
    }
    
    /**
     * Treats a &lt;ExprArith&gt; at the top of the stack.
     * 
     * Tries to apply rule [10]&nbsp;&lt;ExprArith&gt;&nbsp;&rarr;&nbsp;&lt;Prod&gt;&lt;ExprArith'&gt;
     * 
     * @return a ParseTree with a &lt;ExprArith&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree exprArith() throws IOException, ParseException{
        switch (current.getType()) {
            case MINUS:
            case LPAREN:
            case VARNAME:
            case NUMBER:
                // [10] <ExprArith>  ->  <Prod> <ExprArith'>
                ruleOutput(10,"<ExprArith>","<Prod> <ExprArith'>");
                return new ParseTree(NonTerminal.ExprArith, Arrays.asList(
                    prod(),
                    exprArithPrime()
                ));
            default:
                throw new ParseException(current,NonTerminal.ExprArith,Arrays.asList(
                    LexicalUnit.MINUS,
                    LexicalUnit.LPAREN,
                    LexicalUnit.VARNAME,
                    LexicalUnit.NUMBER
                ));
        }
    }

    /**
     * Treats a &lt;ExprArith'&gt; at the top of the stack.
     * 
     * Tries to apply one of the rules <ul>
     *   <li>[11]&nbsp;&lt;ExprArith'&gt;&nbsp;&rarr;&nbsp;<code>+</code>&lt;Prod&gt;&lt;ExprArith'&gt;</li>
     *   <li>[12]&nbsp;&lt;ExprArith'&gt;&nbsp;&rarr;&nbsp;<code>-</code>&lt;Prod&gt;&lt;ExprArith'&gt;</li>
     *   <li>[13]&nbsp;&lt;ExprArith'&gt;&nbsp;&rarr;&nbsp;&epsilon;</li>
     * </ul>
     * 
     * @return a ParseTree with a &lt;ExprArith'&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree exprArithPrime() throws IOException, ParseException{
        switch (current.getType()) {
            // [11] <ExprArith'>  ->  + <Prod> <ExprArith'>
            case PLUS:
                ruleOutput(11,"<ExprArith'>","+ <Prod> <ExprArith'>");
                return new ParseTree(NonTerminal.ExprArithPrime, Arrays.asList(
                    match(LexicalUnit.PLUS),
                    prod(),
                    exprArithPrime()
                ));
            // [12] <ExprArith'>  ->  - <Prod> <ExprArith'>
            case MINUS:
                ruleOutput(12,"<ExprArith'>","- <Prod> <ExprArith'>");
                return new ParseTree(NonTerminal.ExprArithPrime, Arrays.asList(
                    match(LexicalUnit.MINUS),
                    prod(),
                    exprArithPrime()
                ));
            // [13] <ExprArith'>  ->  EPSILON
            case SEMI:
            case RPAREN:
            case RBRACK:
            case EQUAL:
            case SMALEQ:
            case SMALLER:
            case IMPLIES:
            case PIPE:
                ruleOutput(13,"<ExprArith'>","ɛ");
                return new ParseTree(NonTerminal.ExprArithPrime, Arrays.asList(
                    new ParseTree(LexicalUnit.EPSILON)
                ));
            default:
                throw new ParseException(current,NonTerminal.ExprArithPrime,Arrays.asList(
                    LexicalUnit.PLUS,
                    LexicalUnit.MINUS,
                    LexicalUnit.SEMI,
                    LexicalUnit.RPAREN,
                    LexicalUnit.RBRACK,
                    LexicalUnit.EQUAL,
                    LexicalUnit.SMALEQ,
                    LexicalUnit.SMALLER,
                    LexicalUnit.IMPLIES,
                    LexicalUnit.PIPE
                ));
        }
    }
    
    /**
     * Treats a &lt;Prod&gt; at the top of the stack.
     * 
     * Tries to apply rule [14]&nbsp;&lt;Prod&gt;&nbsp;&rarr;&nbsp;&lt;Atom&gt;&lt;Prod'&gt;
     * 
     * @return a ParseTree with a &lt;Prod&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree prod() throws IOException, ParseException{
        switch (current.getType()) {
            case MINUS:
            case LPAREN:
            case VARNAME:
            case NUMBER:
                // [14] <Prod>  ->  <Atom> <Prod'>
                ruleOutput(18,"<Prod'>","<Atom> <Prod'>");
                return new ParseTree(NonTerminal.Prod, Arrays.asList(
                    atom(),
                    prodPrime()
                ));
            default:
                throw new ParseException(current,NonTerminal.ExprArith,Arrays.asList(
                    LexicalUnit.MINUS,
                    LexicalUnit.LPAREN,
                    LexicalUnit.VARNAME,
                    LexicalUnit.NUMBER
                ));
        }
    }

    /**
     * Treats a &lt;Prod'&gt; at the top of the stack.
     * 
     * Tries to apply one of the rules <ul>
     *   <li>[19]&nbsp;&lt;Prod'&gt;&nbsp;&rarr;&nbsp;<code>*</code>&lt;Atom&gt;&lt;Prod'&gt;</li>
     *   <li>[20]&nbsp;&lt;Prod'&gt;&nbsp;&rarr;&nbsp;<code>/</code>&lt;Atom&gt;&lt;Prod'&gt;</li>
     *   <li>[21]&nbsp;&lt;Prod'&gt;&nbsp;&rarr;&nbsp;&epsilon;</li>
     * </ul>
     * 
     * @return a ParseTree with a &lt;Prod'&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree prodPrime() throws IOException, ParseException{
        switch (current.getType()) {
            // [15] <Prod'>  ->  * <Atom> <Prod'>
            case TIMES:
                ruleOutput(15,"<Prod'>","* <Atom> <Prod'>");
                return new ParseTree(NonTerminal.ProdPrime, Arrays.asList(
                    match(LexicalUnit.TIMES),
                    atom(),
                    prodPrime()
                ));
            // [16] <Prod'>  ->  / <Atom> <Prod'>
            case DIVIDE:
                ruleOutput(16,"<Prod'>","/ <Atom> <Prod'>");
                return new ParseTree(NonTerminal.ProdPrime, Arrays.asList(
                    match(LexicalUnit.DIVIDE),
                    atom(),
                    prodPrime()
                ));
            // [17] <Prod'>  ->  EPSILON
            case SEMI:
            case PLUS:
            case MINUS:
            case RPAREN:
            case RBRACK:
            case EQUAL:
            case SMALLER:
            case SMALEQ:
            case IMPLIES:
            case PIPE:
                ruleOutput(17,"<Prod'>","ɛ");
                return new ParseTree(NonTerminal.ProdPrime, Arrays.asList(
                    new ParseTree(LexicalUnit.EPSILON)
                ));
            default:
                throw new ParseException(current,NonTerminal.ProdPrime,Arrays.asList(
                    LexicalUnit.PLUS,
                    LexicalUnit.MINUS,
                    LexicalUnit.TIMES,
                    LexicalUnit.DIVIDE,
                    LexicalUnit.SEMI,
                    LexicalUnit.RPAREN,
                    LexicalUnit.RBRACK,
                    LexicalUnit.SMALEQ,
                    LexicalUnit.IMPLIES,
                    LexicalUnit.PIPE,
                    LexicalUnit.EQUAL,
                    LexicalUnit.SMALLER
                ));
        }
    }

    /**
     * Treats a &lt;Atom&gt; at the top of the stack.
     * 
     * Tries to apply one of the rules <ul>
     *   <li>[18]&nbsp;&lt;Atom&gt;&nbsp;&rarr;&nbsp;[VarName]</li>
     *   <li>[19]&nbsp;&lt;Atom&gt;&nbsp;&rarr;&nbsp;[Number]</li>
     *   <li>[20]&nbsp;&lt;Atom&gt;&nbsp;&rarr;&nbsp;<code>(</code>&lt;ExprArith&gt;<code>)</code></li>
     *   <li>[21]&nbsp;&lt;Atom&gt;&nbsp;&rarr;&nbsp;<code>-</code>&lt;Atom&gt;</li>
     * </ul>
     * 
     * @return a ParseTree with a &lt;Atom&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree atom() throws IOException, ParseException{
        switch (current.getType()) {
            // [21] <Atom>  ->  - <Atom>
            case MINUS:
                ruleOutput(21,"<Atom>","- <Atom>");
                return new ParseTree(NonTerminal.Atom, Arrays.asList(
                    match(LexicalUnit.MINUS),
                    atom()
                ));
            // [20] <Atom>  ->  (<ExprArith>)
            case LPAREN:
                ruleOutput(20,"<Atom>","(<ExprArith>)");
                return new ParseTree(NonTerminal.Atom, Arrays.asList(
                    match(LexicalUnit.LPAREN),
                    exprArith(),
                    match(LexicalUnit.RPAREN)
                ));
            // [18] <Atom>  ->  [VarName]
            case VARNAME:
                ruleOutput(18,"<Atom>","[VarName]");
                return new ParseTree(NonTerminal.Atom, Arrays.asList(
                    match(LexicalUnit.VARNAME)
                ));
            // [19] <Atom>  ->  [Number]
            case NUMBER:
                ruleOutput(19,"<Atom>","[Number]");
                return new ParseTree(NonTerminal.Atom, Arrays.asList(
                    match(LexicalUnit.NUMBER)
                ));
            default:
                throw new ParseException(current,NonTerminal.Atom,Arrays.asList(
                    LexicalUnit.MINUS,
                    LexicalUnit.LPAREN,
                    LexicalUnit.VARNAME,
                    LexicalUnit.NUMBER
                ));
        }
    }
    
    /**
     * Treats a &lt;If&gt; at the top of the stack.
     * 
     * Tries to apply rule [22]&nbsp;&lt;If&gt;&nbsp;&rarr;&nbsp;<code>IF</code>&nbsp;<code>{</code>&lt;Cond&gt;<code>}</code>&nbsp;<code>THEN</code>&nbsp;&lt;Code&gt;&nbsp;<code>ELSE</code>&nbsp;&lt;IfTail&gt;
     * 
     * @return a ParseTree with a &lt;If&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree ifExpr() throws IOException, ParseException{
        // [22] <If>  -> If {<Cond>} Then <Code><IfTail>
        ruleOutput(22,"<If>","If {<Cond>} Then <Code><IfTail>");
        return new ParseTree(NonTerminal.If, Arrays.asList(
            match(LexicalUnit.IF),
            match(LexicalUnit.LBRACK),
            cond(),
            match(LexicalUnit.RBRACK),
            match(LexicalUnit.THEN),
            code(),
            ifTail()
        ));
    }

    /**
     * Treats a &lt;IfTail&gt; at the top of the stack.
     * 
     * Tries to apply one of the rules <ul>
     *   <li>[23]&nbsp;&lt;IfTail&gt;&nbsp;&rarr;&nbsp;<code>End</code></li>
     *   <li>[24]&nbsp;&lt;IfTail&gt;&nbsp;&rarr;&nbsp;<code>Else</code>&nbsp;&lt;Instruction&gt;&nbsp;<code>End</code></li>
     * </ul>
     * 
     * @return a ParseTree with a &lt;IfTail&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree ifTail() throws IOException, ParseException{
        switch (current.getType()) {
            // [24] <IfTail>  ->  Else <Code> End
            case ELSE:
                ruleOutput(24,"<IfTail>","Else <Code> End");
                return new ParseTree(NonTerminal.IfTail, Arrays.asList(
                    match(LexicalUnit.ELSE),
                    code(),
                    match(LexicalUnit.END)
                ));
            // [23] <IfTail>  ->  End
            case END:
                ruleOutput(23,"<IfTail>","END");
                return new ParseTree(NonTerminal.IfTail, Arrays.asList(
                    match(LexicalUnit.END)
                ));
            default:
                throw new ParseException(current,NonTerminal.IfTail,Arrays.asList(
                    LexicalUnit.END,
                    LexicalUnit.ELSE
                ));
        }
    }
    
    /**
     * Treats a &lt;Cond&gt; at the top of the stack.
     * 
     * Tries to apply rule [25]&nbsp;&lt;Cond&gt;&nbsp;&rarr;&nbsp;&lt;SimpleCond&gt;&lt;Cond'&gt;
     * 
     * @return a ParseTree with a &lt;Cond&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree cond() throws IOException, ParseException{
        switch (current.getType()) {
            case MINUS:
            case LPAREN:
            case PIPE:
            case VARNAME:
            case NUMBER:
                // [25] <Cond>  -> <SimpleCond> <Cond'>
                ruleOutput(25,"<Cond>","<SimpleCond> <Cond'>");
                return new ParseTree(NonTerminal.Cond, Arrays.asList(
                    simpleCond(),
                    condPrime()
                ));
            default:
                throw new ParseException(current,NonTerminal.ExprArith,Arrays.asList(
                    LexicalUnit.MINUS,
                    LexicalUnit.LPAREN,
                    LexicalUnit.PIPE,
                    LexicalUnit.VARNAME,
                    LexicalUnit.NUMBER
                ));
        }
    }
    
    /**
     * Treats a &lt;Cond'&gt; at the top of the stack.
     * 
     * Tries to apply one of the rules <ul>
     *   <li>[26]&nbsp;&lt;Cond'&gt;&nbsp;&rarr;&nbsp;<code>-></code> &lt;Cond'&gt;</li>
     *   <li>[27]&nbsp;&lt;Cond'&gt;&nbsp;&rarr;&nbsp;&epsilon;</li>
     * </ul>
     * 
     * @return a ParseTree with a &lt;Cond'&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree condPrime() throws IOException, ParseException{
        switch (current.getType()) {
            // [26] <Cond'>  ->  -> <Cond>
            case IMPLIES:
                ruleOutput(26,"<Cond'>","-> <Cond>");
                return new ParseTree(NonTerminal.CondPrime, Arrays.asList(
                    match(LexicalUnit.IMPLIES),
                    cond()
                ));
            // [27] <Cond'>  ->  EPSILON
            case PIPE:
            case RBRACK:
                ruleOutput(27,"<Cond'>","ɛ");
                return new ParseTree(NonTerminal.CondPrime, Arrays.asList(
                    new ParseTree(LexicalUnit.EPSILON)
                ));
            default:
                throw new ParseException(current,NonTerminal.CondPrime,Arrays.asList(
                    LexicalUnit.IMPLIES,
                    LexicalUnit.PIPE,
                    LexicalUnit.RBRACK
                ));
        }
    }
    
    /**
     * Treats a &lt;SimpleCond&gt; at the top of the stack.
     * 
     * Tries to apply one of the rules <ul>
     *   <li>[28]&nbsp;&lt;SimpleCond&gt;&nbsp;&rarr;&nbsp;<code>|</code>&lt;Cond&gt;<code>|</code></li>
     *   <li>[29]&nbsp;&lt;SimpleCond&gt;&nbsp;&rarr;&nbsp;&lt;ExprArith&gt;&lt;Comp&gt;&lt;ExprArith&gt;</li>
     * </ul>
     * 
     * @return a ParseTree with a &lt;SimpleCond&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree simpleCond() throws IOException, ParseException{
        switch (current.getType()) {
            // [28] <SimpleCond>  ->  |<Cond>|
            case PIPE:
                ruleOutput(35,"<SimpleCond>","|<Cond>|");
                return new ParseTree(NonTerminal.SimpleCond, Arrays.asList(
                    match(LexicalUnit.PIPE),
                    cond(),
                    match(LexicalUnit.PIPE)
                ));
            // [29] <SimpleCond>  ->  <ExprArith> <Comp> <ExprArith>
            case MINUS:
            case LPAREN:
            case VARNAME:
            case NUMBER:
                ruleOutput(36,"<SimpleCond>","<ExprArith> <Comp> <ExprArith>");
                return new ParseTree(NonTerminal.SimpleCond, Arrays.asList(
                    exprArith(),
                    compOp(),
                    exprArith()
                ));
            default:
                throw new ParseException(current,NonTerminal.SimpleCond,Arrays.asList(
                    LexicalUnit.PIPE,
                    LexicalUnit.MINUS,
                    LexicalUnit.LPAREN,
                    LexicalUnit.VARNAME,
                    LexicalUnit.NUMBER
                ));
        }
    }
                
    /**
     * Treats a &lt;Comp&gt; at the top of the stack.
     * 
     * Tries to apply one of the rules <ul>
     *   <li>[30]&nbsp;&lt;Comp&gt;&nbsp;&rarr;&nbsp;<code>==</code></li>
     *   <li>[31]&nbsp;&lt;Comp&gt;&nbsp;&rarr;&nbsp;<code>&lt;=</code></li>
     *   <li>[32]&nbsp;&lt;Comp&gt;&nbsp;&rarr;&nbsp;<code>&lt;</code></li>
     * </ul>
     * 
     * @return a ParseTree with a &lt;Comp&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree compOp() throws IOException, ParseException{
        switch (current.getType()) {
            // [30] <Comp>  ->  ==
            case EQUAL:
                ruleOutput(30,"<Comp>","==");
                return new ParseTree(NonTerminal.Comp, Arrays.asList(
                    match(LexicalUnit.EQUAL)
                ));
            // [31] <Comp>  ->  <=
            case SMALEQ:
                ruleOutput(31,"<Comp>","<=");
                return new ParseTree(NonTerminal.Comp, Arrays.asList(
                    match(LexicalUnit.SMALEQ)
                ));
            // [32] <Comp>  ->  <
            case SMALLER:
                ruleOutput(32,"<Comp>","<");
                return new ParseTree(NonTerminal.Comp, Arrays.asList(
                    match(LexicalUnit.SMALLER)
                ));
            default:
                throw new ParseException(current,NonTerminal.Comp,Arrays.asList(
                    LexicalUnit.EQUAL,
                    LexicalUnit.SMALEQ,
                    LexicalUnit.SMALLER
                ));
        }
    }
    
    /**
     * Treats a &lt;While&gt; at the top of the stack.
     * 
     * Tries to apply rule [33]&nbsp;&lt;While&gt;&nbsp;&rarr;&nbsp;<code>While</code>&nbsp;<code>{</code>&lt;Cond&gt;<code>}</code>&nbsp;<code>Do</code>&nbsp;&lt;Code&gt;&nbsp;<code>End</code>
     * 
     * @return a ParseTree with a &lt;While&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree whileExpr() throws IOException, ParseException{
       
        ruleOutput(33,"<While>","While <Cond> Do <Code>");
        return new ParseTree(NonTerminal.While, Arrays.asList(
            match(LexicalUnit.WHILE),
            match(LexicalUnit.LBRACK),
            cond(),
            match(LexicalUnit.RBRACK),
            match(LexicalUnit.DO),
            code(),
            match(LexicalUnit.END)
        ));
    }
    
    /**
     * Treats a &lt;Output&gt; at the top of the stack.
     * 
     * Tries to apply rule [34]&nbsp;&lt;Output&gt;&nbsp;&rarr;&nbsp;<code>Print(</code>[Varname]<code>)</code>
     * 
     * @return a ParseTree with a &lt;Output&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree outputExpr() throws IOException, ParseException{
        // [34] <Output>  ->  Print([VarName])
        ruleOutput(34,"<Output>","Print([VarName])");
        return new ParseTree(NonTerminal.Output, Arrays.asList(
            match(LexicalUnit.PRINT),
            match(LexicalUnit.LPAREN),
            match(LexicalUnit.VARNAME),
            match(LexicalUnit.RPAREN)
        ));
    }

    /**
     * Treats a &lt;Input&gt; at the top of the stack.
     * 
     * Tries to apply rule [34]&nbsp;&lt;Input&gt;&nbsp;&rarr;&nbsp;<code>Input(</code>[Varname]<code>)</code>
     * 
     * @return a ParseTree with a &lt;Input&gt; non-terminal at the root.
     * @throws IOException in case the lexing fails (syntax error).
     * @throws ParseException in case the parsing fails (syntax error).
     */
    private ParseTree inputExpr() throws IOException, ParseException{
        // [34] <Input>  ->  Input([VarName])
        ruleOutput(34,"<Input>","Input([VarName])");
        return new ParseTree(NonTerminal.Input, Arrays.asList(
            match(LexicalUnit.INPUT),
            match(LexicalUnit.LPAREN),
            match(LexicalUnit.VARNAME),
            match(LexicalUnit.RPAREN)
        ));
    }

/*
    private ParseTree nonterminal() throws IOException, ParseException{
        return new ParseTree(NonTerminal.TODO); // TODO
    }
*/
}
