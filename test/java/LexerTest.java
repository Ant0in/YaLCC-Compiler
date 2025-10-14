
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.io.StringReader;

/**
 * Unit tests for the LexicalAnalyzer class.
 * These tests cover various scenarios including valid tokens, comments,
 * whitespace handling, and invalid tokens.
 */
public class LexerTest {

    /**
     * Test the lexical analyzer (lexer) for correct tokenization of a regular input string.
     */
    @Test
    public void testTokenScan() throws Exception {
        // sample code to be tokenized (from pdf example)
        String code = "Input(a);";
        LexicalAnalyzer lexer = new LexicalAnalyzer(new StringReader(code));

        // token Input
        Symbol token = lexer.yylex();
        assertEquals(LexicalUnit.INPUT, token.getType());
        assertEquals("Input", token.getValue());

        // token (
        token = lexer.yylex();
        assertEquals(LexicalUnit.LPAREN, token.getType());

        // token a
        token = lexer.yylex();
        assertEquals(LexicalUnit.VARNAME, token.getType());
        assertEquals("a", token.getValue());

        // token )
        token = lexer.yylex();
        assertEquals(LexicalUnit.RPAREN, token.getType());

        // token ;
        token = lexer.yylex();
        assertEquals(LexicalUnit.SEMI, token.getType());

        // token end of stream (EOS)
        token = lexer.yylex();
        assertEquals(LexicalUnit.EOS, token.getType());
    }

    /**
     * Test that spaces and tabs are ignored by the lexer.
     */
    @Test
    public void testSpacesAndTabs() throws Exception {

        // sample code with spaces and tabs
        String code = "   Prog   MyProgram\tIs ;  ";
        LexicalAnalyzer lexer = new LexicalAnalyzer(new StringReader(code));

        // token Prog
        Symbol token = lexer.yylex();
        assertEquals(LexicalUnit.PROG, token.getType());

        // token MyProgram
        token = lexer.yylex();
        assertEquals(LexicalUnit.PROGNAME, token.getType());
        assertEquals("MyProgram", token.getValue());

        // token Is
        token = lexer.yylex();
        assertEquals(LexicalUnit.IS, token.getType());

        // token ;
        token = lexer.yylex();
        assertEquals(LexicalUnit.SEMI, token.getType());

        // token end of stream (EOS)
        token = lexer.yylex();
        assertEquals(LexicalUnit.EOS, token.getType());
    }

    /**
     * Test that short comments (starting with $) are ignored by the lexer.
     */
    @Test
    public void testShortCommentIgnored() throws Exception {

        // sample code with a short comment
        String code = "Input(a); $ this is a comment\nPrint(a);";
        LexicalAnalyzer lexer = new LexicalAnalyzer(new StringReader(code));

        // token Input
        Symbol token = lexer.yylex();
        assertEquals(LexicalUnit.INPUT, token.getType());

        // skip LPAREN, VARNAME, RPAREN, SEMI
        for(int i=0;i<4;i++) lexer.yylex();

        // token Print (should have skipped the comment)
        token = lexer.yylex();
        assertEquals(LexicalUnit.PRINT, token.getType());
    }

    /**
     * Test that long comments (enclosed in !!) are ignored by the lexer.
     */
    @Test
    public void testLongCommentIgnored() throws Exception {

        // sample code with a long comment
        String code = "!! this is a long comment \n yeah long !!\nProg Test Is";
        LexicalAnalyzer lexer = new LexicalAnalyzer(new StringReader(code));

        // token Prog (should have skipped the comment)
        Symbol token = lexer.yylex();
        assertEquals(LexicalUnit.PROG, token.getType());

        // no need to check further tokens for this test
    }

    /**
     * Test that invalid tokens throw an exception.
     */
    @Test
    public void testInvalidTokens() throws Exception {
       
        // sample code with an invalid token (@)
        String code = "Input(@);";
        LexicalAnalyzer lexer = new LexicalAnalyzer(new StringReader(code));

        // Expect a RuntimeException when trying to read the invalid token (Line 1, column 7)
        assertThrows(RuntimeException.class, () -> {
            while (lexer.yylex() != null) {
                // continue reading tokens until exception
            }
        });
    }

    /**
     * Test a more complex sequence of tokens to ensure the lexer handles multiple token types correctly.
     */
    @Test
    public void testComplexSequence() throws Exception {

        // sample code with multiple tokens
        String code = "Prog MyTest Is Input(a); Print(a); End";
        LexicalAnalyzer lexer = new LexicalAnalyzer(new StringReader(code));

        // expected tokens in order
        LexicalUnit[] expected = {
            LexicalUnit.PROG, LexicalUnit.PROGNAME, LexicalUnit.IS,
            LexicalUnit.INPUT, LexicalUnit.LPAREN, LexicalUnit.VARNAME, LexicalUnit.RPAREN, LexicalUnit.SEMI,
            LexicalUnit.PRINT, LexicalUnit.LPAREN, LexicalUnit.VARNAME, LexicalUnit.RPAREN, LexicalUnit.SEMI,
            LexicalUnit.END
        };

        // check each token in sequence
        for(LexicalUnit unit : expected) {
            Symbol token = lexer.yylex();
            assertEquals(unit, token.getType());
        }
    }
}
