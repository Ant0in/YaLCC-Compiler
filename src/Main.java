package src;

import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -cp bin src.Main <sourceFile>");
            return;
        }

        String filename = args[0];
        try {
            LexicalAnalyzer lexer = new LexicalAnalyzer(new FileReader(filename));
            Symbol token;

            System.out.println("Liste des tokens :");
            while ((token = lexer.next_token()) != null) {
                System.out.println(token);
                if (token.getType() == LexicalUnit.EOS) break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
