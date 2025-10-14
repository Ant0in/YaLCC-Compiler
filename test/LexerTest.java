import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.io.StringReader;

public class LexerTest {

    @Test
    public void testInputToken() throws Exception {
        String code = "Input(a);";
        LexicalAnalyzer lexer = new LexicalAnalyzer(new StringReader(code));

        Symbol token = lexer.yylex();
        assertEquals(LexicalUnit.INPUT, token.getType());
        assertEquals("Input", token.getValue());

        token = lexer.yylex();
        assertEquals(LexicalUnit.LPAREN, token.getType());

        token = lexer.yylex();
        assertEquals(LexicalUnit.VARNAME, token.getType());
        assertEquals("a", token.getValue());

        token = lexer.yylex();
        assertEquals(LexicalUnit.RPAREN, token.getType());

        token = lexer.yylex();
        assertEquals(LexicalUnit.SEMI, token.getType());
    }
}
