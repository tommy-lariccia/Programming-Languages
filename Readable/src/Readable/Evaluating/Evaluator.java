package Readable.Evaluating;

import Readable.Environments.Environment;
import Readable.LexicalAnalysis.Lexeme;
import Readable.LexicalAnalysis.Types;

import Readable.Readable;

import static Readable.LexicalAnalysis.Types.*;

public class Evaluator {
    // ----------- Evaluating -----------
    public Lexeme eval(Lexeme tree, Environment env) {
        if (tree == null) return new Lexeme(Types.NULL);
        return switch (tree.getType()) {
            case PROG, RETURN -> eval(tree.getChild(0), env);
            case STATEMENT_LIST -> evalStatementList(tree, env);
            case INT_LIT, TRUE, FALSE, FLOAT_LIT, STRING_LIT -> tree;
            case IDENTIFIER -> env.lookup(tree);
            case ASS -> evalAss(tree, env);
            case PLUS, MINUS, TIMES, DIVIDE, NEGATE, GREATER_THAN_COMP, GREATER_OR_EQUAL_COMP,
                    LESS_OR_EQUAL_COMP, LESS_THAN_COMP, EQUALITY_COMP, NOT_EQUAL_COMP, AND, OR -> evalExpr(tree, env);
            case NOT -> CrossTypeOperations.notComp(eval(tree, env));
            case FOREACH -> evalForeach(tree, env);
            case FUNC -> evalFunctionDefinition(tree, env);
            case FUNC_CALL -> evalFunctionCall(tree, env);
            case WHILE -> evalWhileLoop(tree, env);

            default -> error("Cannot evaluate " + tree, tree.getLine());
        };
    }

    private Lexeme evalStatementList(Lexeme tree, Environment env) {
        Lexeme result = new Lexeme(Types.NULL);
        for (Lexeme statement : tree.getChildren()) {
            result = eval(statement, env);
        }
        return result;
    }

    private Lexeme evalAss(Lexeme tree, Environment env) {
        Lexeme expr = eval(tree.getChild(2), env);
        Types type = tree.getChild(0).getType();
        Lexeme name = tree.getChild(1);
        env.addOrUpdate(type, name, expr);
        return new Lexeme(null);
    }

    private Lexeme evalForeach(Lexeme tree, Environment env) {
        Lexeme type = tree.getChild(0);
        Lexeme iden = tree.getChild(1);
        Lexeme iter = tree.getChild(2);
        Lexeme block = tree.getChild(3);
        for (Lexeme lex : toIterable(iter, env).getChildren()) {
            Environment subEnv = new Environment(env);
            subEnv.add(type.getType(), iden, lex);
            eval(block, subEnv);
        }
        return new Lexeme(Types.NULL);
    }

    private Lexeme evalWhileLoop(Lexeme tree, Environment env) {
        Lexeme comp = tree.getChild(0);
        Lexeme block = tree.getChild(1);
        while (true) {
            Lexeme evalComp = eval(comp, env);
            if (evalComp.getType() != TRUE && evalComp.getType() != FALSE)
                error("While loop condition must evaluate to TRUE or FALSE", tree.getLine());
            if (evalComp.getType() == TRUE) {
                Environment callEnv = new Environment(env);
                eval(block, callEnv);
            } else {
                break;
            }
        }

        return new Lexeme(NULL);
    }

    private Lexeme toIterable(Lexeme tree, Environment env) {
        if (tree.getType() == Types.IDENTIFIER) {
            return toIterable(eval(tree, env), env);
        } else if (tree.getType() == Types.ARR) {
            if (tree.getChild(0).getType() == Types.EMPTY_LIST) {
                return new Lexeme(Types.ARR);
            }
            return tree.getChild(0);
        } else if (tree.getType() == Types.STRING_LIT) {
            Lexeme arr = new Lexeme(Types.ARR);
            for (char c : tree.getStringValue().toCharArray()) {
                arr.addChild(new Lexeme(Types.STRING_LIT, -1, String.valueOf(c)));
            }
            return arr;
        } else if (tree.getType() == Types.INT_LIT) {
            Lexeme arr = new Lexeme(Types.ARR);
            for (int i = 0; i < tree.getIntValue(); i++) {
                arr.addChild(new Lexeme(Types.INT_LIT, -1, i));
            }
            return arr;
        } else if (tree.getType() == Types.RANGE) {
            Lexeme arr = new Lexeme(Types.ARR);
            for (int i = tree.getChild(0).getIntValue(); i < tree.getChild(1).getIntValue(); i++) {
                arr.addChild(new Lexeme(Types.INT_LIT, -1, i));
            }
            return arr;
        }
        return new Lexeme(Types.NULL);
    }

    private Lexeme evalFunctionDefinition(Lexeme tree, Environment env) {
        tree.setDefiningEnv(env);
        Lexeme functionName = tree.getChild(1);
        env.add(tree.getType(), functionName, tree);
        return functionName;
    }

    private Lexeme evalFunctionCall(Lexeme tree, Environment env) {
        Lexeme functionName = tree.getChild(0);
        Lexeme funcDefTree = env.lookup(functionName);
        Lexeme paramList = funcDefTree.getChild(2);
        Lexeme argList = tree.getChild(1);
        Lexeme evaluatedArgList = evalArgList(argList, env);
        if (evaluatedArgList.getChildren().size() != paramList.getChildren().size())
            error("Expected " + paramList.getChildren().size() + " children supplied to function call, but " +
                    "received " + evaluatedArgList.getChildren().size() + ".", tree.getLine());
        Environment callEnv = new Environment(funcDefTree.getDefiningEnv());
        for (int i = 0; i < paramList.getChildren().size(); i++) {
            callEnv.localAdd(paramList.getChild(i).getChild(0).getType(), paramList.getChild(i).getChild(1),
                    evaluatedArgList.getChild(i));
        }
        Lexeme funcBody = funcDefTree.getChild(3);
        return eval(funcBody, callEnv);
    }

    private Lexeme evalArgList(Lexeme params, Environment env) {
        Lexeme root = new Lexeme(PARAM_LIST);
        for (Lexeme param : params.getChildren()) {
            root.addChild(eval(param, env));
        }
        return root;
    }

    private Lexeme evalExpr(Lexeme tree, Environment env) {
        if (tree.getType() == NEGATE) tree.addChild(new Lexeme(INT_LIT, tree.getLine(), -1));
        Lexeme side1 = eval(tree.getChild(0), env);
        Lexeme side2 = eval(tree.getChild(1), env);
        switch (tree.getType()) {
            case PLUS -> {return CrossTypeOperations.handleAddition(side1, side2);}
            case MINUS -> {return CrossTypeOperations.handleSubtraction(side1, side2);}
            case TIMES, NEGATE -> {return CrossTypeOperations.handleMultiplication(side1, side2);}
            case DIVIDE -> {return CrossTypeOperations.handleDivision(side1, side2);}
            case GREATER_THAN_COMP -> {return CrossTypeOperations.greaterThan(side1, side2);}
            case EQUALITY_COMP -> {return CrossTypeOperations.equalityComp(side1, side2);}
            case NOT_EQUAL_COMP -> {return CrossTypeOperations.notEqualComp(side1, side2);}
            case LESS_THAN_COMP -> {return CrossTypeOperations.lessThanComp(side1, side2);}
            case LESS_OR_EQUAL_COMP -> {return CrossTypeOperations.lessThanOrEqualToComp(side1, side2);}
            case GREATER_OR_EQUAL_COMP -> {return CrossTypeOperations.greaterThanOrEqualToComp(side1, side2);}
            case AND -> {return CrossTypeOperations.andComp(side1, side2);}
            case OR -> {return CrossTypeOperations.orComp(side1, side2);}

            default -> {return new Lexeme(Types.NULL);}
        }
    }

    // ----------- Error Reporting -----------
    private Lexeme error(String message, Lexeme lex) {
        Readable.runtimeError(message, lex);
        return new Lexeme(Types.ERROR, lex.getLine(), message);
    }

    private Lexeme error(String message, int lineNumber) {
        Readable.runtimeError(message, lineNumber);
        return new Lexeme(Types.ERROR, lineNumber, message);
    }
}
