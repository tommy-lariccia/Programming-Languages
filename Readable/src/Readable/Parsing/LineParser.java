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
        Lexeme root = new Lexeme(STATEMENT_LIST);
        while (statementPending())
            root.addChild(progStatement());
        return root;

            return error("A statement should be either start a block (conditional, function, loop), call a function, or " +
                    "initialize a variable or lambda function. Likely an expression.");
        }
    }

    public Lexeme statementList() {
        Lexeme root = new Lexeme(STATEMENT_LIST);
        while (statementPending())
            root.addChild(progStatement());
        return root;
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

    // TYPING

    private Lexeme typing() {
        if (typingArrPending()) {
            return typingArr();
        } else if (typingKeywordsPending()) {
            return typingKeywords();
        } else {
            return error("Expected a valid type but did not receive one.");
        }
    }

    private Lexeme typingArr() {
        Lexeme root = new Lexeme(TYPING_ARR);
        root.addChild(typingKeywords());
        consume(OSQUARE);
        consume(CSQUARE);
        return root;
    }

    private Lexeme typingKeywords() {
        if (check(INTEGER)) return consume(INTEGER);
        else if (check(STRING)) return consume(STRING);
        else if (check(BOOL)) return consume(BOOL);
        else if (check(FLOAT)) return consume(FLOAT);
        else return error("Expected 'int', 'float', 'bool', or 'str'");
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
        if (typingPending()) root.addChild(typing());
        else root.addChild(new Lexeme(ANY_TYPE));
        root.addChild(consume(IDENTIFIER));
        consume(ASSIGN);
        root.addChild(expression());
        return root;
    }

    private Lexeme arrSlotAssign() {
        Lexeme root = new Lexeme(ARR_ASS);
        if (typingArrPending()) root.addChild(typingArr());
        else root.addChild(new Lexeme(ANY_TYPE));
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
        return binaryExpression();
    }

    private Lexeme binaryExpression() {
        Lexeme left = unaryExpression();
        if (binaryOpPending()) {
            Lexeme root = binaryOp();
            root.addChild(left);
            root.addChild(binaryExpression());
            return root;
        } else {
            return left;
        }
    }

    private Lexeme binaryOp() {
        ArrayList<Types> binOps = new ArrayList<>();
        binOps.addAll(List.of(new Types[]{TIMES, DIVIDE, MINUS, PLUS, AND, OR, EQUALITY_COMP, GREATER_OR_EQUAL_COMP, GREATER_THAN_COMP, LESS_THAN_COMP, LESS_OR_EQUAL_COMP}));
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
        Lexeme lex = new Lexeme(FUNC_CALL);
        lex.addChild(consume(IDENTIFIER));
        consume(OPAREN);
        if (!check(CPAREN)) lex.addChild(argList());
        else lex.addChild(new Lexeme(EMPTY_LIST));
        consume(CPAREN);
        return lex;
    }

    private Lexeme argList() {
        Lexeme root = new Lexeme(ARG_LIST);
        root.addChild(expression());
        if (check(COMMA)) {
            consume(COMMA);
            root.addAllChildren(argList().getChildren());
        }
        return root;
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
        if (typingPending()) root.addChild(typing());
        else root.addChild(new Lexeme(ANY_TYPE));
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
        if (typingPending()) root.addChild(typing());
        else root.addChild(new Lexeme(ANY_TYPE));
        root.addChild(consume(IDENTIFIER));
        consume(OPAREN);
        if (!check(CPAREN)) root.addChild(paramList());
        else root.addChild(new Lexeme(EMPTY_LIST));
        consume(CPAREN);
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
        if (typingPending()) {
            root.addChild(typing());
        } else {
            root.addChild(new Lexeme(ANY_TYPE));
        }
        root.addChild(consume(IDENTIFIER));
        consume(IN);
        root.addChild(iterable());
        return root;
    }

    private Lexeme iterable() {
        if (arrPending()) {
            return arr();
        } else if (check(INT_LIT)) {
            if (checkNext(RANGE)) return range();
            else return consume(INT_LIT);
        } else if (check(IDENTIFIER)) {
            return consume(IDENTIFIER);
        } else {
            return error("Expected array, range, or integer but did not receive either.");
        }
    }

    private Lexeme range() {
        Lexeme op1 = consume(INT_LIT);
        Lexeme root = consume(RANGE);
        root.addChild(op1);
        root.addChild(consume(INT_LIT));
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

    private boolean typingKeywordsPending() {
        return check(INTEGER) || check(FLOAT) || check(STRING) || check(BOOL);
    }

    private boolean typingArrPending() {
        return typingKeywordsPending() && checkNext(OSQUARE);
    }

    private boolean assignmentPending() {
        return (typingPending()) || (check(IDENTIFIER) && checkNext(ASSIGN));
    }

    private boolean typingPending() {
        return typingKeywordsPending();
    }

    private boolean arrSlotAssignPending() {  // only necessarily ugly function
        return (check(IDENTIFIER) && checkNext(OSQUARE) && !checkNextNext(CSQUARE)) ||
                (typingArrPending() && checkNextNext(CSQUARE) && lexemes.get(nextLexemeIndex + 2).getType() == IDENTIFIER
                        && lexemes.get(nextLexemeIndex + 3).getType() == OSQUARE && lexemes.get(nextLexemeIndex + 4).getType() != CSQUARE);
    }

    private boolean exprListPending() {
        return expressionPending();
    }

    private boolean expressionPending() {
        return terminalExpressionPending();
    }

    private boolean binaryOpPending() {
        ArrayList<Types> binOps = new ArrayList<>();
        binOps.addAll(List.of(new Types[]{TIMES, DIVIDE, MINUS, PLUS, AND, OR, EQUALITY_COMP, GREATER_OR_EQUAL_COMP, GREATER_THAN_COMP, LESS_THAN_COMP, LESS_OR_EQUAL_COMP}));
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
