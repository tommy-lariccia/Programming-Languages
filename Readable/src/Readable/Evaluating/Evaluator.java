package Readable.Evaluating;

import Readable.Environments.Environment;
import Readable.LexicalAnalysis.Lexeme;
import Readable.LexicalAnalysis.Types;

import Readable.Readable;

public class Evaluator {
    // ----------- Evaluating -----------
    public Lexeme eval(Lexeme tree, Environment env) {
        if (tree == null) return new Lexeme(Types.NULL);
        return switch (tree.getType()) {
            case PROG -> eval(tree.getChild(0), env);
            case STATEMENT_LIST -> evalStatementList(tree, env);
            case INT_LIT, TRUE, FALSE, FLOAT_LIT, STRING_LIT -> tree;
            case IDENTIFIER -> env.lookup(tree);
            case ASS -> evalAss(tree, env);
            case PLUS, MINUS, TIMES, DIVIDE -> evalExpr(tree, env);
            case FOREACH -> evalForeach(tree, env);


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
        if (type == Types.INTEGER)
            type = Types.INT_LIT;
        if (type == Types.FLOAT)
            type = Types.FLOAT_LIT;
        if (type == Types.STRING)
            type = Types.STRING_LIT;
        env.doInAss(type, tree.getChild(1), expr);
        return new Lexeme(null);
    }

    private Lexeme evalExpr(Lexeme tree, Environment env) {
        Lexeme side1 = eval(tree.getChild(0), env);
        Lexeme side2 = eval(tree.getChild(1), env);
        switch (tree.getType()) {
            case PLUS -> {return handleAddition(side1, side2);}
            case MINUS -> {return handleSubtraction(side1, side2);}
            default -> {return new Lexeme(Types.NULL);}
        }
    }

    private Lexeme evalForeach(Lexeme tree, Environment env) {
        Lexeme type = tree.getChild(0);
        Lexeme iden = tree.getChild(1);
        Lexeme iter = tree.getChild(2);
        Lexeme block = tree.getChild(3);
        System.out.println(iter);
        System.out.println(toIterable(iter, env).getChildren());
        for (Lexeme lex : toIterable(iter, env).getChildren()) {
            Environment subEnv = new Environment(env);
            subEnv.add(type.getType(), iden, lex);
            eval(block, subEnv);
        }
        return new Lexeme(Types.NULL);
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

    // ----------- Cross-Type Operations -----------

    private Lexeme handleAddition(Lexeme s1, Lexeme s2) {
        switch (s1.getType()) {
            case INT_LIT -> {
                switch (s2.getType()) {
                    case INT_LIT -> {return new Lexeme(Types.INT_LIT, -1, s1.getIntValue() + s2.getIntValue());}
                    case FLOAT_LIT -> {return new Lexeme(Types.FLOAT_LIT, -1, s1.getIntValue() + s2.getDecValue());}
                    case STRING_LIT -> {return new Lexeme(Types.STRING_LIT, -1, String.valueOf(s1.getIntValue()) + s2.getStringValue());}
                    default -> {return new Lexeme(Types.NULL);}
                }
            }
            case FLOAT_LIT -> {
                switch (s2.getType()) {
                    case INT_LIT -> {return new Lexeme(Types.FLOAT_LIT, -1, s1.getDecValue() + s2.getIntValue());}
                    case FLOAT_LIT -> {return new Lexeme(Types.FLOAT_LIT, -1, s1.getDecValue() + s2.getDecValue());}
                    case STRING_LIT -> {return new Lexeme(Types.STRING_LIT, -1, String.valueOf(s1.getDecValue()) + s2.getStringValue());}
                    default -> {return new Lexeme(Types.NULL);}
                }
            }
            case STRING_LIT -> {
                switch (s2.getType()) {
                    case STRING_LIT -> {return new Lexeme(Types.STRING_LIT, -1, s1.getStringValue() + s2.getStringValue());}
                    case FLOAT_LIT, INT_LIT -> {return new Lexeme(Types.STRING_LIT, -1, s1.getStringValue() + String.valueOf(s2.getStringValue()));}
                    default -> {return new Lexeme(Types.NULL);}
                }
            }
        }
        return new Lexeme(Types.NULL);
    }

    private Lexeme handleSubtraction(Lexeme s1, Lexeme s2) {
        switch (s1.getType()) {
            case INT_LIT -> {
                switch (s1.getType()) {
                    case INT_LIT -> {return new Lexeme(Types.INT_LIT, -1, s1.getIntValue() - s2.getIntValue());}
                    case FLOAT_LIT -> {return new Lexeme(Types.FLOAT_LIT, -1, s1.getIntValue() - s2.getDecValue());}
                }
            }
            case FLOAT_LIT -> {
                switch (s1.getType()) {
                    case INT_LIT -> {return new Lexeme(Types.FLOAT_LIT, -1, s1.getDecValue() - s2.getIntValue());}
                    case FLOAT_LIT -> {return new Lexeme(Types.FLOAT_LIT, -1, s1.getDecValue() - s2.getDecValue());}
                }
            }
        }
        if (s1.getType() == Types.STRING_LIT && s2.getType() == Types.INT_LIT) {
            if (s1.getStringValue().length() > s2.getIntValue())
                return new Lexeme(Types.STRING_LIT, -1, s1.getStringValue().substring(0, s1.getStringValue().length() - s2.getIntValue()));
            else
                return new Lexeme(Types.STRING_LIT, -1, "");
        }
        return new Lexeme(Types.NULL);
    }

    private Lexeme handleMultiplication(Lexeme s1, Lexeme s2) {
        
        return new Lexeme(Types.NULL);
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
