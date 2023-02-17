// Written by Tommy Lariccia at the Westminster Schools
// Code instruction provided by Mr (Mitchell) Griest and Mr (Jonathan) Lusth.

package src;

import src.LexicalAnalysis.Lexeme;
import src.LexicalAnalysis.Lexer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Readable {
    public static void main(String[] args) throws Exception {  // TODO: Move back to IOException
        try {
            if (args.length == 1) runFile(args[0]);
            else {
                System.out.println("Usage: Readable [path to .read file]");
                System.exit(64);
            }
        } catch (Exception exception) {
            throw new Exception(exception.toString());
        }
    }

    private static String getSourceCodeFromFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        return new String(bytes, Charset.defaultCharset());
    }

    private static void runFile(String path) throws Exception {
        System.out.println("Running " + path + "...");
        String source = getSourceCodeFromFile(path);

        // Lexing
        Lexer lexer = new Lexer(source);
        ArrayList<Lexeme> lexemes = lexer.lex();
        System.out.println(lexemes);

    }

}
