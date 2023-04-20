package Readable.Evaluating.Library;

import Readable.LexicalAnalysis.Lexeme;
import Readable.LexicalAnalysis.Types;

import Readable.Readable;

import java.util.ArrayList;
import java.util.stream.StreamSupport;

import Readable.Environments.Environment;

import Readable.Evaluating.Evaluator;


import static Readable.LexicalAnalysis.Types.*;

public class BuiltIns {  // TODO: Break into files
    // ----------- Static Functions -----------
    // ----------- General -----------
    public static Lexeme print(ArrayList<Lexeme> args, int line, Environment env) {
        args = evaluateArgs(args, env);
        if (args.size() != 1) {
            error("Expected " + 1 + " children supplied to function call, but " +
                    "received " + args.size() + ".", line);
            return new Lexeme(NULL);
        }
        System.out.println(args.get(0).printRepr());
        return new Lexeme(NULL);
    }

    public static Lexeme type(ArrayList<Lexeme> args, int line, Environment env) {
        args = evaluateArgs(args, env);
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

    // ----------- Iterables -----------
    public static Lexeme len(ArrayList<Lexeme> args, int line, Environment env) {
        args = evaluateArgs(args, env);
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

    // ----------- Arithmetic -----------
    public static Lexeme sum(ArrayList<Lexeme> args, int line, Environment env) {
        args = evaluateArgs(args, env);
        if (args.size() < 2)
            return error("Built-in 'sum' function takes two or more arguments.", line);
        else if (Arithmetic.checkSumUnavailable(args.get(0))) {
            return error("Cannot perform addition with lexeme of type " + args.get(0).getType(), line);
        }
        Lexeme sum_ = args.get(0);
        for (int i = 1; i < args.size(); i++) {
            sum_ = Arithmetic.sum(sum_, args.get(i));
            if (sum_ == null) {
                if (Arithmetic.checkSumUnavailable(args.get(i))) return error("Cannot perform addition with lexeme of type " + args.get(i).getType(), line);
                else return error("Cannot perform addition between lexeme of type " + sum_.getType() + " and lexeme of type " + args.get(i).getType(), line);
            }
        }
        return sum_;
    }

    public static Lexeme subtract(ArrayList<Lexeme> args, int line, Environment env) {
        args = evaluateArgs(args, env);
        if (args.size() != 2)
            return error("Built-in 'subtract' function takes exactly two arguments.", line);
        else if (Arithmetic.checkSubtractionUnavailable(args.get(0))) {
            return error("Cannot perform subtraction with lexeme of type " + args.get(0).getType(), line);
        } else if (Arithmetic.checkSubtractionUnavailable(args.get(1))) {
            return error("Cannot perform subtraction with lexeme of type " + args.get(1).getType(), line);
        }
        Lexeme result = Arithmetic.subtract(args.get(0), args.get(1));
        if (result == null)
            return error("Cannot perform subtraction between lexeme of type " + args.get(0).getType() + " and lexeme of type " + args.get(1).getType(), line);
        return result;
    }

    public static Lexeme multiply(ArrayList<Lexeme> args, int line, Environment env) {
        args = evaluateArgs(args, env);
        if (args.size() < 2)
            return error("Built-in 'multiply' function takes two or more arguments.", line);
        else if (Arithmetic.checkMultiplicationUnavailable(args.get(0))) {
            return error("Cannot perform multiplication with lexeme of type " + args.get(0).getType(), line);
        }
        Lexeme product = args.get(0);
        for (int i = 1; i < args.size(); i++) {
            product = Arithmetic.multiply(product, args.get(i));
            if (product == null) {
                if (Arithmetic.checkMultiplicationUnavailable(args.get(i))) return error("Cannot perform multiplication with lexeme of type " + args.get(i).getType(), line);
                else return error("Cannot perform multiplication between lexeme of type " + product.getType() + " and lexeme of type " + args.get(i).getType(), line);
            }
        }
        return product;
    }

    public static Lexeme divide(ArrayList<Lexeme> args, int line, Environment env) {
        args = evaluateArgs(args, env);
        if (args.size() != 2)
            return error("Built-in 'divide' function takes exactly two arguments.", line);
        else if (Arithmetic.checkDivisionUnavailable(args.get(0))) {
            return error("Cannot perform division with lexeme of type " + args.get(0).getType(), line);
        } else if (Arithmetic.checkDivisionUnavailable(args.get(1))) {
            return error("Cannot perform division with lexeme of type " + args.get(1).getType(), line);
        }
        Lexeme result = Arithmetic.divide(args.get(0), args.get(1));
        if (result == null)
            return error("Cannot perform division between lexeme of type " + args.get(0).getType() + " and lexeme of type " + args.get(1).getType(), line);
        return result;
    }

    // ----------- Comparator -----------
    public static Lexeme greaterThan(ArrayList<Lexeme> args, int line, Environment env) {
        args = evaluateArgs(args, env);
        if (args.size() != 2)
            return error("Built-in 'greater_than' operator takes exactly two arguments.", line);
        Lexeme result = Comparator.greaterThan(args.get(0), args.get(1));
        if (result == null)
            return error("Cannot compare between lexeme of type " + args.get(0).getType() + " and lexeme of type " + args.get(1).getType(), line);
        return result;
    }

    public static Lexeme greaterThanOrEqualTo(ArrayList<Lexeme> args, int line, Environment env) {
        args = evaluateArgs(args, env);
        if (args.size() != 2)
            return error("Built-in 'greater_than_or_equal_to' operator takes exactly two arguments.", line);
        Lexeme result = Comparator.greaterThanOrEqualToComp(args.get(0), args.get(1));
        if (result == null)
            return error("Cannot compare between lexeme of type " + args.get(0).getType() + " and lexeme of type " + args.get(1).getType(), line);
        return result;
    }

    public static Lexeme lessThan(ArrayList<Lexeme> args, int line, Environment env) {
        args = evaluateArgs(args, env);

        if (args.size() != 2)
            return error("Built-in 'less_than' operator takes exactly two arguments.", line);
        Lexeme result = Comparator.lessThanComp(args.get(0), args.get(1));
        if (result == null)
            return error("Cannot compare between lexeme of type " + args.get(0).getType() + " and lexeme of type " + args.get(1).getType(), line);
        return result;
    }

    public static Lexeme lessThanOrEqualTo(ArrayList<Lexeme> args, int line, Environment env) {
        args = evaluateArgs(args, env);
        if (args.size() != 2)
            return error("Built-in 'less_than_or_equal_to' operator takes exactly two arguments.", line);
        Lexeme result = Comparator.lessThanOrEqualToComp(args.get(0), args.get(1));
        if (result == null)
            return error("Cannot compare between lexeme of type " + args.get(0).getType() + " and lexeme of type " + args.get(1).getType(), line);
        return result;
    }

    public static Lexeme equal(ArrayList<Lexeme> args, int line, Environment env) {
        args = evaluateArgs(args, env);
        if (args.size() != 2)
            return error("Built-in 'equal_to' operator takes exactly two arguments.", line);
        Lexeme result = Comparator.equalityComp(args.get(0), args.get(1));
        if (result == null)
            return error("Cannot compare between lexeme of type " + args.get(0).getType() + " and lexeme of type " + args.get(1).getType(), line);
        return result;
    }

    public static Lexeme notEqual(ArrayList<Lexeme> args, int line, Environment env) {
        args = evaluateArgs(args, env);
        if (args.size() != 2)
            return error("Built-in 'not_equal_to' operator takes exactly two arguments.", line);
        Lexeme result = Comparator.notEqualComp(args.get(0), args.get(1));
        if (result == null)
            return error("Cannot compare between lexeme of type " + args.get(0).getType() + " and lexeme of type " + args.get(1).getType(), line);
        return result;
    }

    // ----------- Boolean -----------
    public static Lexeme AND(ArrayList<Lexeme> args, int line, Environment env) {
        args = evaluateArgs(args, env);
        if (args.size() != 2)
            return error("Built-in 'AND' operator takes exactly two arguments.", line);
        return Boolean.andComp(args.get(0), args.get(1));
    }

    public static Lexeme OR(ArrayList<Lexeme> args, int line, Environment env) {
        args = evaluateArgs(args, env);
        if (args.size() != 2)
            return error("Built-in 'OR' operator takes exactly two arguments.", line);
        return Boolean.orComp(args.get(0), args.get(1));
    }

    public static Lexeme NOT(ArrayList<Lexeme> args, int line, Environment env) {
        args = evaluateArgs(args, env);
        if (args.size() != 1)
            return error("Built-in 'NOT' operator takes exactly one argument.", line);
        return Boolean.notComp(args.get(0));
    }

    public static Lexeme truthy(ArrayList<Lexeme> args, int line, Environment env) {
        args = evaluateArgs(args, env);
        if (args.size() != 1)
            return error("Built-in 'truthy' function takes exactly one argument.", line);
        return Boolean.truthy(args.get(0));
    }

    // ----------- Helper(s) -----------
    private static ArrayList<Lexeme> evaluateArgs(ArrayList<Lexeme> args, Environment env) {
        ArrayList<Lexeme> newList = new ArrayList<>();
        for (Lexeme lex : args) newList.add((new Evaluator()).eval(lex, env));  // TODO: Make evaluator static
        return newList;
    }

    // ----------- Errors -----------
    private static Lexeme error(String message, Lexeme lex) {
        Readable.runtimeError(message, lex);
        return new Lexeme(Types.ERROR, lex.getLine(), message);
    }

    private static Lexeme error(String message, int lineNumber) {
        Readable.runtimeError(message, lineNumber);
        return new Lexeme(Types.ERROR, lineNumber, message);
    }
}
