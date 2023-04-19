package Readable.Environments;

import Readable.LexicalAnalysis.Lexeme;
import Readable.LexicalAnalysis.Types;

import Readable.Readable;

import java.util.ArrayList;

import static Readable.LexicalAnalysis.Types.*;

public class BuiltIns {
    // ----------- Static Functions -----------
    public static Lexeme print(ArrayList<Lexeme> args, int line) {
        if (args.size() != 1) {
            error("Expected " + 1 + " children supplied to function call, but " +
                    "received " + args.size() + ".", line);
            return new Lexeme(NULL);
        }
        System.out.println(args.get(0).printRepr());
        return new Lexeme(NULL);
    }

    public static Lexeme type(ArrayList<Lexeme> args, int line) {
        if (args.size() != 1) {
            error("Expected " + 1 + " children supplied to function call, but " +
                    "received " + args.size() + ".", line);
            return new Lexeme(NULL);
        }
        Lexeme tree = args.get(0);
        Types type = tree.getType();
        if (type == Types.INT_LIT)
            return new Lexeme(Types.STRING_LIT, tree.getLine(), "int");
        if (type == Types.STRING_LIT)
            return new Lexeme(Types.STRING_LIT, tree.getLine(), "str");
        if (type == Types.FLOAT_LIT)
            return new Lexeme(Types.STRING_LIT, tree.getLine(), "float");
        if (type == Types.ARR)
            return new Lexeme(Types.STRING_LIT, tree.getLine(), "arr");
        if (type == Types.NULL)
            return new Lexeme(Types.STRING_LIT, tree.getLine(), "null");
        if (type == Types.TRUE || type == Types.FALSE)
            return new Lexeme(Types.STRING_LIT, tree.getLine(), "bool");
        if (type == Types.FUNC)
            return new Lexeme(Types.STRING_LIT, tree.getLine(), "func");
        if (type == Types.BUILT_IN_FUNC)
            return new Lexeme(STRING_LIT, tree.getLine(), "bltIn");
        error("Cannot evaluate built-in 'type' on value of type " + tree.getType().toString(),
                tree.getLine());
        return new Lexeme(NULL);
    }

    public static Lexeme len(ArrayList<Lexeme> args, int line) {
        if (args.size() != 1) {
            error("Expected " + 1 + " children supplied to function call, but " +
                    "received " + args.size() + ".", line);
            return new Lexeme(NULL);
        }
        Lexeme tree = args.get(0);
        if (tree.getType() == STRING_LIT) {
            return new Lexeme(INT_LIT, tree.getLine(), tree.getStringValue().length());
        } else if (tree.getType() == ARR) {
            return new Lexeme(INT_LIT, tree.getLine(), tree.getChild(0).getChildren().size());
        } else {
            error("Cannot evaluate built-in 'lex' on value of type " + tree.getType().toString(),
                    tree.getLine());
        }
        return new Lexeme(NULL);
    }

    private static Lexeme error(String message, Lexeme lex) {
        Readable.runtimeError(message, lex);
        return new Lexeme(Types.ERROR, lex.getLine(), message);
    }

    private static Lexeme error(String message, int lineNumber) {
        Readable.runtimeError(message, lineNumber);
        return new Lexeme(Types.ERROR, lineNumber, message);
    }

}
