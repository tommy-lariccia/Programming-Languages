package Readable.Evaluating;

import Readable.Environments.Environment;
import Readable.LexicalAnalysis.Lexeme;
import Readable.LexicalAnalysis.Types;

import Readable.Readable;

public class Evaluator {


//    public Lexeme eval(Lexeme lex, Environment env) {
//        System.out.println(lex);
//        switch (lex.getType()) {
//            case PROG -> {
//                return eval(lex.getChild(0), env);
//            }
//            case STATEMENT_LIST -> {
//                return evalStatementList(lex, env);
//            }
//        }
//        return null;
//    }
//
//    public Lexeme evalStatementList(Lexeme lex, Environment env) {
//        for (Lexeme statement : lex.getChildren()) {
//            eval(statement, env);
//        }
//        return null;
//    }
//
//    public Lexeme evalAss(Lexeme lex, Environment env) {
//        Lexeme idem = lex.getChild(1);
//        Lexeme value = eval(lex.getChild(1), env);
//        switch (lex.getChild(0).getType()) {
//            case LOCAL -> {
//                if (env.softLookup(idem) != null)
//                    return error("Runtime Error: 'Local' declarations cannot be made if identifier already set in local scope",
//                            lex);
//                else
//                    env.add(Types.ANY_TYPE, idem, value);
//                    return null;
//            }
//            case ANY_TYPE -> {if (env.lookup())}
//            default -> {}
//        }
//    }
//
//    // ----------- Error Reporting -----------
//    private Lexeme error(String message, Lexeme lex) {
//        Readable.runtimeError(message, lex);
//        return new Lexeme(Types.ERROR, lex.getLine(), message);
//    }
}
