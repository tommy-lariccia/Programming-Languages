// Written by Tommy Lariccia at the Westminster Schools
// Code instruction provided by Mr (Mitchell) Griest and Mr (Jonathan) Lusth.

package Readable;

import Readable.Environments.Environment;
import Readable.Environments.TestEnvironment;
import Readable.Evaluating.Evaluator;
import Readable.LexicalAnalysis.Lexeme;
import Readable.LexicalAnalysis.Lexer;
import Readable.Parsing.Parser;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Readable {
    private static final ArrayList<String> syntaxErrorMessages = new ArrayList<>();
    private static final ArrayList<String> runtimeErrorMessages = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        try {
            if (args.length == 1) runFile(args[0]);
            else {
                System.out.println("Usage: Readable [path to .read file]");
                System.exit(64);
            }
        } catch (IOException exception) {
            throw new IOException(exception.toString());
        }
        printErrors();
    }

    private static String getSourceCodeFromFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        return new String(bytes, Charset.defaultCharset());
    }

    private static void runFile(String path) throws IOException {
        System.out.println("Running " + path + "...");
        String source = getSourceCodeFromFile(path);

        // Lexing
        Lexer lexer = new Lexer(source);
        ArrayList<Lexeme> lexemes = lexer.lex();

        // Recognizing
        Parser parsing = new Parser(lexemes);
        Lexeme parseTree = parsing.program();
//        parseTree.printAsParseTree();

        // Environments
        Environment globalEnvironment = new Environment();

        // Evaluation
        Evaluator evaluator = new Evaluator();
        evaluator.eval(parseTree, globalEnvironment);
        System.out.println(globalEnvironment);
    }

    // ------------ Errors ------------

    public static void syntaxError(String message, int lineNumber) {
        syntaxErrorMessages.add("Syntax Error (line " + lineNumber + "): " + message);
    }

    public static void syntaxError(String message, Lexeme lexeme) {
        syntaxErrorMessages.add("Syntax error at " + lexeme + ": " + message);
    }

    public static void runtimeError(String message, int lineNumber) {
        syntaxErrorMessages.add("Runtime Error (line " + lineNumber + "): " + message);
        printErrors();
        System.exit(65);
    }

    public static void runtimeError(String message, Lexeme lexeme) {
        syntaxErrorMessages.add("Runtime error at " + lexeme + ": " + message);
        printErrors();
        System.exit(65);
    }

    private static void printErrors() {
        final String ANSI_YELLOW = "\u001B[33m";
        final String ANSI_RED_BACKGROUND = "\u001B[41m";
        final String ANSI_RESET = "\u001B[0m";

        for (String syntaxErrorMessage : syntaxErrorMessages) {
            System.out.println(ANSI_YELLOW + syntaxErrorMessage + ANSI_RESET);
        }

        for (String runtimeErrorMessage : runtimeErrorMessages) {
            System.out.println(ANSI_RED_BACKGROUND + runtimeErrorMessage + ANSI_RESET);
        }
    }

}
