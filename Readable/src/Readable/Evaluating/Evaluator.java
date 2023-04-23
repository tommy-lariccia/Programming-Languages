package Readable.Evaluating;

import Readable.Environments.Environment;
import Readable.Environments.NamedValue;
import Readable.LexicalAnalysis.Lexeme;
import Readable.LexicalAnalysis.Types;

import Readable.Evaluating.Library.BuiltIns;
import Readable.Readable;

import java.util.ArrayList;
import java.util.Arrays;

import static Readable.LexicalAnalysis.Types.*;

public class Evaluator {
    // ----------- Instance Variables -----------
    boolean returnDone = false;

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
            case NOT -> BuiltIns.NOT(new ArrayList<>(Arrays.asList(tree)), tree.getLine(), env);
            case FOREACH -> evalForeach(tree, env);
            case FUNC -> evalFunctionDefinition(tree, env);
            case FUNC_CALL -> evalFunctionCall(tree, env);
            case WHILE -> evalWhileLoop(tree, env);
            case ARR -> evalArr(tree, env);
            case LAMBDA -> evalLambdaInitialization(tree, env);
            case CONDITIONAL_BLOCK -> evalCond(tree, env);
            case ARR_ACC -> arrAcc(tree, env);
            case ARR_ASS -> arrAss(tree, env);

            default -> defaultEval(tree, env);
        };
    }

    private Lexeme defaultEval(Lexeme tree, Environment env) {
        error("Cannot evaluate " + tree, tree.getLine()); return new Lexeme(NULL);}

    private Lexeme evalStatementList(Lexeme tree, Environment env) {
        Lexeme result = new Lexeme(Types.NULL);
        for (Lexeme statement : tree.getChildren()) {
            if (statement.getType() == RETURN) {
                returnDone = true;
                return eval(statement, env);
            }
            result = eval(statement, env);
            if (returnDone)
                return result;   // propagates upwards; it's sort of clever for such a poorly-planned system (on my part)
        }
        return result;
    }

    private Lexeme evalAss(Lexeme tree, Environment env) {
        Lexeme name = tree.getChild(0);
        Lexeme expr = eval(tree.getChild(1), env);
        env.addOrUpdate( name, expr);
        return new Lexeme(NULL);
    }

    private Lexeme evalForeach(Lexeme tree, Environment env) {
        Lexeme iden = tree.getChild(0);
        Lexeme iter = tree.getChild(1);
        Lexeme block = tree.getChild(2);
        for (Lexeme lex : toIterable(iter, env).getChildren()) {
            Environment subEnv = new Environment(env);
            subEnv.add(iden, lex);
            eval(block, subEnv);
        }
        return new Lexeme(Types.NULL);
    }

    private Lexeme evalWhileLoop(Lexeme tree, Environment env) {
        Lexeme comp = tree.getChild(0);
        Lexeme block = tree.getChild(1);
        while (true) {
            ArrayList<Lexeme> terms = new ArrayList<>();
            terms.add(comp);
            Lexeme evalComp = BuiltIns.truthy(terms, tree.getLine(), env);
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
            return rangeToArr(tree, env);
        }
        return new Lexeme(Types.NULL);
    }

    private Lexeme rangeToArr(Lexeme tree, Environment env) {
        Lexeme first = eval(tree.getChild(0), env);
        Lexeme second = eval(tree.getChild(1), env);
        Lexeme arr = new Lexeme(Types.ARR);
        if (first.getType() != INT_LIT)
            error("RANGE operands must be integers.", first);
        if (second.getType() != INT_LIT)
            error("RANGE operands must be integers.", second);
        if (first.getIntValue() < second.getIntValue()) {
            for (int i = first.getIntValue(); i < second.getIntValue(); i++) arr.addChild(new Lexeme(Types.INT_LIT, -1, i));
        } else {
            for (int i = first.getIntValue(); i > second.getIntValue(); i--) arr.addChild(new Lexeme(Types.INT_LIT, -1, i));
        }
        return arr;
    }

    private Lexeme evalFunctionDefinition(Lexeme tree, Environment env) {
        Environment newEnv = getFunctionEnv(env);
        tree.setDefiningEnv(newEnv);
        Lexeme functionName = tree.getChild(0);
        env.add(functionName, tree);
        newEnv.add(functionName, tree);
        return functionName;
    }

    private Environment getFunctionEnv(Environment env) {
        Environment newEnv = new Environment();
        if (env.isGlobal()) {
            for (NamedValue v : env.seeEntries()) {
                if (v.getValue().getType() == FUNC) {newEnv.add(v.getName().copy(), v.getValue().copy());}
            }
        } else {newEnv = env.copy();}
        return newEnv;
    }

    private Lexeme evalLambdaInitialization(Lexeme tree, Environment env) {
        Lexeme functionName = tree.getChild(0);
        Lexeme paramList = tree.getChild(1);
        Lexeme returnExpr = tree.getChild(2);
        Lexeme newFunc = new Lexeme(FUNC);
        Lexeme returnStatement = new Lexeme(RETURN);
        returnStatement.addChild(returnExpr);
        newFunc.addChild(functionName);
        newFunc.addChild(paramList);
        newFunc.addChild(returnStatement);
        evalFunctionDefinition(newFunc, env);
        return functionName;
    }

    private Lexeme getFuncTreeFromCall(Lexeme tree, Environment env) {
        Lexeme firstChild = tree.getChild(0);
        if (firstChild.getType() == IDENTIFIER)
            return env.lookup(firstChild);
        else if (firstChild.getType() == FUNC_CALL) {
            Lexeme possFunc = eval(firstChild, env);
            if (possFunc.getType() == FUNC)
                return possFunc;
            else
                return error("Cannot call a lexeme of type " + possFunc.getType() + " as a function", tree.getLine());
        }
        return error("Cannot call a lexeme of type " + firstChild.getType() + " as a function", tree.getLine());
    }

    private Lexeme evalFunctionCall(Lexeme tree, Environment env) {
        Lexeme funcDefTree = getFuncTreeFromCall(tree, env);
        Lexeme argList = getArgList(tree.getChild(1), env);
        if (funcDefTree.getType() == FUNC) {
            Lexeme evaluatedArgList = evalArgList(argList, env);
            return evalLexicalFunction(tree, env, funcDefTree, evaluatedArgList);
        } else if (funcDefTree.getType() == BUILT_IN_FUNC) {
            return funcDefTree.getBuiltInFunc().call(argList.getChildren(), tree.getLine(), env);
        }
        error("Cannot call a lexeme of type " + funcDefTree.getType() + " as a function", tree.getLine());
        return new Lexeme(NULL);
    }

    private Lexeme getArgList(Lexeme tree, Environment env) {
        Lexeme allArgs = new Lexeme(ARG_LIST);
        for (Lexeme arg : tree.getChildren()) {
            if (arg.getType() == UNPACKABLE) {
                Lexeme child = arg.getChild(0);
                if (child.getType() == IDENTIFIER)
                    child = env.lookup(child);
                if (child.getType() != ARR)
                    error("Can only use the unpack operator (*) on arrays.", tree);
                for (Lexeme subArg : child.getChild(0).getChildren()) allArgs.addChild(subArg);
            } else
                allArgs.addChild(arg);
        }
        return allArgs;
    }

    private Lexeme evalLexicalFunction(Lexeme tree, Environment env, Lexeme funcDefTree, Lexeme evaluatedArgList) {
        Environment callEnv = new Environment(funcDefTree.getDefiningEnv());
        if (funcDefTree.getChild(1).getType() == ARB_PARAM_LIST) {
            Lexeme arr = new Lexeme(ARR);
            arr.addChild(new Lexeme(EXPR_LIST));
            for (Lexeme lex : evaluatedArgList.getChildren()) arr.getChild(0).addChild(lex);
            callEnv.add(funcDefTree.getChild(1).getChild(0), arr);
        } else
            callEnv = normalParamPopulate(tree, env, funcDefTree, evaluatedArgList);
        Lexeme funcBody = funcDefTree.getChild(2);
        Lexeme evaluated = eval(funcBody, callEnv);
        returnDone = false;
        return evaluated;
    }

    private Environment normalParamPopulate(Lexeme tree, Environment env, Lexeme funcDefTree, Lexeme evaluatedArgList) {
        Lexeme paramList = funcDefTree.getChild(1);
        if (evaluatedArgList.getChildren().size() != paramList.getChildren().size())
            error("Expected " + paramList.getChildren().size() + " children supplied to function call, but " +
                    "received " + evaluatedArgList.getChildren().size() + ".", tree.getLine());
        Environment callEnv = new Environment(funcDefTree.getDefiningEnv());
        for (int i = 0; i < paramList.getChildren().size(); i++) {
            callEnv.localAdd(paramList.getChild(i).getChild(0).copy(), evaluatedArgList.getChild(i).copy());
        }
        return callEnv;
    }

    private Lexeme evalArgList(Lexeme params, Environment env) {
        Lexeme root = new Lexeme(ARG_LIST);
        for (Lexeme param : params.getChildren()) {
            root.addChild(eval(param, env));
        }
        return root;
    }

    private Lexeme evalExpr(Lexeme tree, Environment env) {
        if (tree.getType() == NEGATE) tree.addChild(new Lexeme(INT_LIT, tree.getLine(), -1));
        switch (tree.getType()) {
            case PLUS -> {return BuiltIns.sum(tree.getChildren(), tree.getChild(0).getLine(), env);}
            case MINUS -> {return BuiltIns.subtract(tree.getChildren(), tree.getChild(0).getLine(), env);}
            case TIMES, NEGATE -> {return BuiltIns.multiply(tree.getChildren(), tree.getChild(0).getLine(), env);}
            case DIVIDE -> {return BuiltIns.divide(tree.getChildren(), tree.getChild(0).getLine(), env);}
            case GREATER_THAN_COMP -> {return BuiltIns.greaterThan(tree.getChildren(), tree.getChild(0).getLine(), env);}
            case EQUALITY_COMP -> {return BuiltIns.equal(tree.getChildren(), tree.getChild(0).getLine(), env);}
            case NOT_EQUAL_COMP -> {return BuiltIns.notEqual(tree.getChildren(), tree.getChild(0).getLine(), env);}
            case LESS_THAN_COMP -> {return BuiltIns.lessThan(tree.getChildren(), tree.getChild(0).getLine(), env);}
            case LESS_OR_EQUAL_COMP -> {return BuiltIns.lessThanOrEqualTo(tree.getChildren(), tree.getChild(0).getLine(), env);}
            case GREATER_OR_EQUAL_COMP -> {return BuiltIns.greaterThanOrEqualTo(tree.getChildren(), tree.getChild(0).getLine(), env);}
            case AND -> {return BuiltIns.AND(tree.getChildren(), tree.getChild(0).getLine(), env);}
            case OR -> {return BuiltIns.OR(tree.getChildren(), tree.getChild(0).getLine(), env);}

            default -> {return new Lexeme(Types.NULL);}
        }
    }

    private Lexeme evalArr(Lexeme tree, Environment env) {
        Lexeme newArr = new Lexeme(ARR);
        Lexeme newExprList = new Lexeme(EXPR_LIST);
        for (Lexeme expr : tree.getChild(0).getChildren()) newExprList.addChild(eval(expr, env));
        newArr.addChild(newExprList);
        return newArr;
    }

    private Lexeme evalCond(Lexeme tree, Environment env) {
        Lexeme ifBlock = tree.getChild(0);
        if (eval(ifBlock.getChild(0), env).getType() == TRUE) {
            return eval(ifBlock.getChild(1), new Environment(env));
        }
        int i = 1;
        while (i < tree.getChildren().size() && tree.getChild(i).getType() == ELSE_IF) {
            Lexeme elseIfBlock = tree.getChild(i);
            if (eval(elseIfBlock.getChild(0), env).getType() == TRUE)
                return eval(elseIfBlock.getChild(1), new Environment(env));
            i++;
        }
        if (tree.getChild(tree.getChildren().size() - 1).getType() == ELSE) {
            Lexeme elseBlock = tree.getChild(tree.getChildren().size() - 1);
            return eval(elseBlock.getChild(0), new Environment(env));
        }
        return new Lexeme(NULL);
    }

    private Lexeme arrAcc(Lexeme tree, Environment env) {
        Lexeme arr = eval(tree.getChild(0), env);
        if (arr.getType() != ARR)
            error("Cannot treat lexeme of type " + arr.getType() + " as an array.", arr.getLine());
        Lexeme index = eval(tree.getChild(1), env);
        if (index.getType() != INT_LIT)
            error("Cannot index into array with lexeme of type " + index.getType() + ".", arr.getLine());
        Lexeme items = arr.getChild(0);
        int realIndex = index.getIntValue();
        if (realIndex < 0)
            realIndex = items.getChildren().size() - Math.abs(realIndex);
        if (0 > realIndex || items.getChildren().size() <= realIndex) {
            return error("Index " + index.getIntValue() + " out of bounds for array of length " + items.getChildren().size(), arr.getLine());
        }
        return arr.getChild(0).getChild(realIndex);
    }

    private Lexeme arrAss(Lexeme tree, Environment env) {
        Lexeme arr = eval(tree.getChild(0), env);
        if (arr.getType() != ARR)
            error("Cannot treat lexeme of type " + arr.getType() + " as an array.", arr.getLine());
        Lexeme index = eval(tree.getChild(1), env);
        if (index.getType() != INT_LIT)
            error("Cannot index into array with lexeme of type " + index.getType() + ".", arr.getLine());
        Lexeme items = arr.getChild(0);
        int realIndex = index.getIntValue();
        if (realIndex < 0)
            realIndex = items.getChildren().size() - Math.abs(realIndex);
        if (0 > realIndex || items.getChildren().size() <= realIndex) {
            error("Index " + index.getIntValue() + " out of bounds for array of length " + items.getChildren().size(), arr.getLine());
        }
        arr.getChild(0).getChildren().set(realIndex, tree.getChild(2));
        return new Lexeme(NULL);
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
