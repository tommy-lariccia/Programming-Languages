package Readable.Parsing;

import Readable.LexicalAnalysis.Lexeme;
import Readable.LexicalAnalysis.Types;
import Readable.Readable;

import java.util.ArrayList;
import java.util.List;

import static Readable.LexicalAnalysis.Types.*;

public class LineParser {
    // ------------ Instance Variables ------------
    private final ArrayList<Lexeme> lexemes;
    private Lexeme currentLexeme;
    private int nextLexemeIndex;

    // ------------ Core Support Methods ------------
    private Types peek() {
        return currentLexeme.getType();
    }

    private boolean check(Types type) {
        return currentLexeme.getType() == type;
    }

    private boolean checkNext(Types type) {
        if (nextLexemeIndex >= lexemes.size()) return false;
        return lexemes.get(nextLexemeIndex).getType() == type;
    }

    private boolean checkNextNext(Types type) {
        if (nextLexemeIndex + 1 >= lexemes.size()) return false;
        return lexemes.get(nextLexemeIndex + 1).getType() == type;
    }

    private Lexeme advance() {
        Lexeme toReturn = currentLexeme;
        currentLexeme = lexemes.get(nextLexemeIndex);
        nextLexemeIndex++;
        return toReturn;
    }

    private Lexeme consume(Types expected) {
        if (check(expected)) {Lexeme toReturn = currentLexeme.copy(); advance(); return toReturn;}
        error("Expected " + expected + " but found " + currentLexeme + ".");
        return new Lexeme(ERROR, currentLexeme.getLine());
    }

    // ------------ Constructor ------------
    public LineParser(ArrayList<Lexeme> lexemeList) {
        lexemes = lexemeList;
        nextLexemeIndex = 0;
        advance();
    }

    // ------------ Consumption Functions ------------
    public Lexeme program() {
        if (statementPending()) return progStatement();
        else {
            return error("A statement should be either start a block (conditional, function, loop), call a function, or " +
                    "initialize a variable or lambda function. Likely an expression.");
        }
    }

    private Lexeme progStatement() {
        if (functionDefinitionPending()) {
            return functionDefinition();
        } else if (check(RETURN)) {
            return returnStatement();
        } else if (check(IDENTIFIER)) {
            if (checkNext(OPAREN)) return functionCall();
            else return variableInitializationAssignment();
        } else if (variableInitializationAssignmentPending()) {
            return variableInitializationAssignment();
        } else if (conditionalStatementPending()) {
            return ifBlock();
        } else if (elifBlockPending()) {
            return elifBlock();
        } else if (elseBlockPending()) {
            return elseBlock();
        } else if (loopStatementPending()) {
            return loopStatement();
        } else if (lambdaInitializationPending()) {
            return lambdaInitialization();
        } else {
            return error("A statement should be either start a block (conditional, function, loop), call a function, or " +
                    "initialize a variable or lambda function.");
        }
    }

    private Lexeme booleanOpts() {
        if (check(TRUE)) return consume(TRUE);
        else if (check(FALSE)) return consume(FALSE);
        else return error("Expected a boolean but received neither 'true' nor 'false'.");
    }

    // ASSIGNS and INITS

    private Lexeme variableInitializationAssignment() {
        if (arrSlotAssignPending()) {
            return arrSlotAssign();
        } else if (assignmentPending()) {
            return assignment();
        } else {
            return error("Malformed variable initialization/assignment.");
        }
    }

    private Lexeme assignment() {
        Lexeme root = new Lexeme(ASS);
        root.addChild(consume(IDENTIFIER));
        consume(ASSIGN);
        root.addChild(expression());
        return root;
    }

    private Lexeme arrSlotAssign() {
        Lexeme root = new Lexeme(ARR_ASS);
        root.addAllChildren(arrAccess().getChildren());
        consume(ASSIGN);
        root.addChild(expression());
        return root;
    }

    private Lexeme arr() {
        Lexeme root = new Lexeme(ARR);
        consume(OSQUARE);
        if (exprListPending()) root.addChild(exprList());
        else root.addChild(new Lexeme(EMPTY_LIST));
        consume(CSQUARE);
        return root;
    }

    private Lexeme arrAccess() {
        Lexeme root = new Lexeme(ARR_ACC);
        root.addChild(consume(IDENTIFIER));
        consume(OSQUARE);
        root.addChild(expression());
        consume(CSQUARE);
        return root;
    }

    // EXPRS

    private Lexeme exprList() {
        Lexeme root = new Lexeme(EXPR_LIST);
        root.addChild(expression());
        if (check(COMMA)) {
            consume(COMMA);
            root.addAllChildren(exprList().getChildren());
        }
        return root;
    }

    private Lexeme expression() {
        return firstBinaryExpression();
    }

    private Lexeme firstBinaryExpression() {
        Lexeme left = secondBinaryExpression();
        if (firstBinaryOpPending()) {
            Lexeme root = firstBinaryOp();
            root.addChild(left);
            root.addChild(firstBinaryExpression());
            return root;
        } else {
            return left;
        }
    }

    private Lexeme secondBinaryExpression() {
        Lexeme left = thirdBinaryExpression();
        if (secondBinaryOpPending()) {
            Lexeme root = secondBinaryOp();
            root.addChild(left);
            root.addChild(secondBinaryExpression());
            return root;
        } else {
            return left;
        }
    }

    private Lexeme thirdBinaryExpression() {
        Lexeme left = fourthBinaryExpression();
        if (thirdBinaryOpPending()) {
            Lexeme root = thirdBinaryOp();
            root.addChild(left);
            root.addChild(thirdBinaryExpression());
            return root;
        } else {
            return left;
        }
    }

    private Lexeme fourthBinaryExpression() {
        Lexeme left = unaryExpression();
        if (fourthBinaryOpPending()) {
            Lexeme root = fourthBinaryOp();
            root.addChild(left);
            root.addChild(fourthBinaryExpression());
            return root;
        } else {
            return left;
        }
    }

    private Lexeme firstBinaryOp() {
        ArrayList<Types> binOps = new ArrayList<>();
        binOps.addAll(List.of(new Types[]{AND, OR, EQUALITY_COMP}));
        if (binOps.contains(peek())) return consume(peek());
        else return error("Expected binary operation but did not receive any.");
    }

    private Lexeme secondBinaryOp() {
        ArrayList<Types> binOps = new ArrayList<>();
        binOps.addAll(List.of(new Types[]{GREATER_OR_EQUAL_COMP, GREATER_THAN_COMP, LESS_THAN_COMP, LESS_OR_EQUAL_COMP}));
        if (binOps.contains(peek())) return consume(peek());
        else return error("Expected binary operation but did not receive any.");
    }

    private Lexeme thirdBinaryOp() {
        ArrayList<Types> binOps = new ArrayList<>();
        binOps.addAll(List.of(new Types[]{MINUS, PLUS}));
        if (binOps.contains(peek())) return consume(peek());
        else return error("Expected binary operation but did not receive any.");
    }

    private Lexeme fourthBinaryOp() {
        ArrayList<Types> binOps = new ArrayList<>();
        binOps.addAll(List.of(new Types[]{TIMES, DIVIDE}));
        if (binOps.contains(peek())) return consume(peek());
        else return error("Expected binary operation but did not receive any.");
    }

    private Lexeme unaryExpression() {
        if (frontUnaryExpressionPending()) {
            return frontUnaryExpression();
        } else if (terminalExpressionPending()) {
            return terminalExpression();
        } else {
            return error("Expected terminal or terminal with unary operator, but did not receive either.");
        }
    }

    private Lexeme frontUnaryExpression() {
        Lexeme root = frontUnaryOperator();
        root.addChild(terminalExpression());
        return root;
    }

    private Lexeme frontUnaryOperator() {
        if (peek() == NOT) {
            return consume(peek());
        } else if (peek() == MINUS) {
            advance();
            return new Lexeme(NEGATE);
        } else return error("Expected '-' or '!' but did not receive either.");
    }

    private Lexeme parenExpression() {
        consume(OPAREN);
        Lexeme root = expression();
        consume(CPAREN);
        return root;
    }

    private Lexeme terminalExpression() {
        if (check(IDENTIFIER)) {
            if (checkNext(OPAREN)) return functionCall();
            else if (checkNext(OSQUARE)) return arrAccess();
            else return consume(IDENTIFIER);
        } else if (check(STRING_LIT)) {
            return consume(STRING_LIT);
        } else if (check(FLOAT_LIT)) {
            return consume(FLOAT_LIT);
        }else if (check(INT_LIT)) {
            return consume(INT_LIT);
        } else if (parenExpressionPending()) {
            return parenExpression();
        } else if (booleanOptsPending()) {
            return booleanOpts();
        } else if (arrPending()) {
            return arr();
        } else if (check(NULL)) {
            return consume(NULL);
        } else {
            return error("Expected some kind of terminal expression, but did not receive.");
        }
    }

    // FUNCS
    private Lexeme functionCall() {
        Lexeme requiredFirst = oneCall();
        Lexeme root = requiredFirst;
        while (check(OPAREN)) {
            Lexeme newRoot = new Lexeme(FUNC_CALL, requiredFirst.getLine());
            newRoot.addChild(root);
            consume(OPAREN);
            if (!check(CPAREN)) newRoot.addChild(argList());
            else newRoot.addChild(new Lexeme(EMPTY_LIST));
            consume(CPAREN);
            root = newRoot;
        }
        return root;
    }

    private Lexeme oneCall() {
        Lexeme lex = new Lexeme(FUNC_CALL, currentLexeme.getLine());
        lex.addChild(consume(IDENTIFIER));
        consume(OPAREN);
        if (!check(CPAREN)) lex.addChild(argList());
        else lex.addChild(new Lexeme(EMPTY_LIST));
        consume(CPAREN);
        return lex;
    }

    private Lexeme argList() {
        Lexeme root = new Lexeme(ARG_LIST);
        root.addChild(argument());
        if (check(COMMA)) {
            consume(COMMA);
            root.addAllChildren(argList().getChildren());
        }
        return root;
    }

    private Lexeme argument() {
        if (check(TIMES)) {
            consume(TIMES);
            Lexeme arg = new Lexeme(UNPACKABLE);
            arg.addChild(expression());
            return arg;
        } else
            return expression();
    }

    private Lexeme paramList() {
        Lexeme root = new Lexeme(PARAM_LIST);
        root.addChild(parameter());
        if (check(COMMA)) {
            consume(COMMA);
            root.addAllChildren(paramList().getChildren());
        }
        return root;
    }

    private Lexeme parameter() {
        Lexeme root = new Lexeme(PARAM);
        root.addChild(consume(IDENTIFIER));
        return root;
    }

    private Lexeme returnStatement() {
        Lexeme root = consume(RETURN);
        root.addChild(expression());
        return root;
    }

    private Lexeme functionDefinition() {
        Lexeme root = consume(FUNC);
        root.addChild(consume(IDENTIFIER));
        consume(OPAREN);
        if (check(TIMES)) root.addChild(arbParamList());
        else if (!check(CPAREN)) root.addChild(paramList());
        else root.addChild(new Lexeme(EMPTY_LIST));
        consume(CPAREN);
        return root;
    }

    private Lexeme arbParamList() {
        Lexeme root = new Lexeme(ARB_PARAM_LIST);
        consume(TIMES);
        root.addChild(consume(IDENTIFIER));
        return root;
    }

    private Lexeme lambdaInitialization() {
        Lexeme root = consume(LAMBDA);
        root.addChild(consume(IDENTIFIER));
        consume(ASSIGN);
        consume(OPAREN);
        if (!check(CPAREN)) root.addChild(paramList());
        else root.addChild(new Lexeme(EMPTY_LIST));
        consume(CPAREN);
        consume(RARROW);
        root.addChild(expression());
        return root;
    }

    // LOOPS

    private Lexeme loopStatement() {
        if (check(WHILE)) {
            return whileLoop();
        } else if (check(FOREACH)) {
            return forEachLoop();
        } else {
            return error("Expected while or for loop");  // this will *never* happen if program's logic is correct
        }
    }

    private Lexeme whileLoop() {
        Lexeme root = consume(WHILE);
        root.addChild(expression());
        return root;
    }

    private Lexeme forEachLoop() {
        Lexeme root = consume(FOREACH);
        root.addChild(consume(IDENTIFIER));
        consume(IN);
        root.addChild(iterable());
        return root;
    }

    private Lexeme iterable() {
        if (arrPending()) {
            return arr();
        } else if (check(INT_LIT) || check(IDENTIFIER)) {
            if (checkNext(COLON)) return consume(peek());
            else return range();
        } else if (expressionPending()) {
            return range();
        } else {
            return error("Expected array, range, or integer but did not receive either.");
        }
    }

    private Lexeme range() {
        Lexeme op1 = expression();
        Lexeme root = consume(RANGE);
        root.addChild(op1);
        root.addChild(expression());
        return root;
    }

    // CONDS

    private Lexeme ifBlock() {
        Lexeme root = consume(IF);
        root.addChild(expression());
        return root;
    }

    private Lexeme elifBlock() {
        consume(ELSE);
        consume(IF);
        Lexeme lex = new Lexeme(ELSE_IF);
        lex.addChild(expression());
        return lex;
    }

    private Lexeme elseBlock() {
        Lexeme root = consume(ELSE);
        return root;
    }


    // ------------ Pending Functions ------------

    private boolean statementPending() {
        return progStatementPending();
    }

    private boolean progStatementPending() {
        return functionDefinitionPending() || functionCallPending() || variableInitializationAssignmentPending()
                || conditionalStatementPending() || loopStatementPending() || lambdaInitializationPending()
                || elseBlockPending() || check(RETURN);
    }

    private boolean functionDefinitionPending() {
        return check(FUNC);
    }

    private boolean functionCallPending() {
        return check(IDENTIFIER) && checkNext(OPAREN);
    }

    private boolean variableInitializationAssignmentPending() {
        return assignmentPending() || arrSlotAssignPending();
    }

    private boolean conditionalStatementPending() {
        return check(IF);
    }

    private boolean loopStatementPending() {
        return check(WHILE) || check(FOREACH);
    }

    private boolean lambdaInitializationPending() {
        return check(LAMBDA);
    }

    private boolean assignmentPending() {
        return check(IDENTIFIER) && checkNext(ASSIGN);
    }

    private boolean arrSlotAssignPending() {
        return (check(IDENTIFIER) && checkNext(OSQUARE) && !checkNextNext(CSQUARE));
    }

    private boolean exprListPending() {
        return expressionPending();
    }

    private boolean expressionPending() {
        return terminalExpressionPending();
    }

    private boolean firstBinaryOpPending() {
        ArrayList<Types> binOps = new ArrayList<>();
//        binOps.addAll(List.of(new Types[]{TIMES, DIVIDE, MINUS, PLUS, AND, OR, EQUALITY_COMP, GREATER_OR_EQUAL_COMP, GREATER_THAN_COMP, LESS_THAN_COMP, LESS_OR_EQUAL_COMP}));
        binOps.addAll(List.of(new Types[]{AND, OR, EQUALITY_COMP}));
        for (Types t : binOps) {
            if (check(t)) return true;
        }
        return false;
    }

    private boolean secondBinaryOpPending() {
        ArrayList<Types> binOps = new ArrayList<>();
        binOps.addAll(List.of(new Types[]{GREATER_OR_EQUAL_COMP, GREATER_THAN_COMP, LESS_THAN_COMP, LESS_OR_EQUAL_COMP}));
        for (Types t : binOps) {
            if (check(t)) return true;
        }
        return false;
    }

    private boolean thirdBinaryOpPending() {
        ArrayList<Types> binOps = new ArrayList<>();
        binOps.addAll(List.of(new Types[]{MINUS, PLUS}));
        for (Types t : binOps) {
            if (check(t)) return true;
        }
        return false;
    }

    private boolean fourthBinaryOpPending() {
        ArrayList<Types> binOps = new ArrayList<>();
        binOps.addAll(List.of(new Types[]{TIMES, DIVIDE}));
        for (Types t : binOps) {
            if (check(t)) return true;
        }
        return false;
    }

    private boolean frontUnaryExpressionPending() {
        return check(MINUS) || check(NOT);
    }

    private boolean terminalExpressionPending() {
        return check(IDENTIFIER) || check(INT_LIT) || check(FLOAT_LIT) || check(STRING_LIT) || check(TRUE)
                || check(FALSE) || check(OPAREN) || check(NULL) || arrPending();
    }

    private boolean parenExpressionPending() {
        return check(OPAREN);
    }

    private boolean booleanOptsPending() {
        return check(TRUE) || check(FALSE);
    }

    private boolean arrPending() {
        return check(OSQUARE);
    }

    private boolean elifBlockPending() {
        return check(ELSE) && checkNext(IF);
    }

    private boolean elseBlockPending() {
        return check(ELSE);
    }

    // ------------ Error Reporting ------------
    private Lexeme error(String message) {
        Readable.syntaxError(message, currentLexeme);
        return new Lexeme(ERROR, currentLexeme.getLine(), message);
    }
}
