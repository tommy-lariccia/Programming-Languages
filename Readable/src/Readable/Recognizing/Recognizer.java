package Readable.Recognizing;

import Readable.LexicalAnalysis.Lexeme;
import Readable.LexicalAnalysis.Types;
import Readable.Readable;

import java.util.ArrayList;
import java.util.List;

import static Readable.LexicalAnalysis.Types.*;

public class Recognizer {
    private static final boolean printDebugMessages = true;

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
        if (check(expected)) return advance();
        error("Expected " + expected + " but found " + currentLexeme + ".");
        return new Lexeme(ERROR, currentLexeme.getLine());
    }

    // ------------ Constructor ------------
    public Recognizer(ArrayList<Lexeme> lexemeList) {
        lexemes = lexemeList;
        nextLexemeIndex = 0;
        advance();
    }

    // ------------ Consumption Functions ------------
    public void program() {
        while (statementPending()) {
            statement();
        }
    }

    private void statement() {
        progStatement();
        consume(NEW_LINE);
    }

    private void progStatement() {
        if (functionDefinitionPending()) {
            functionDefinition();
        } else if (check(IDENTIFIER)) {
            if (checkNext(OPAREN)) functionCall();
            else variableInitializationAssignmentPending();
        } else if (variableInitializationAssignmentPending()) {
            variableInitializationAssignment();
        } else if (conditionalStatementPending()) {
            conditionalStatement();
        } else if (loopStatementPending()) {
            loopStatement();
        } else if (lambdaInitializationPending()) {
            lambdaInitialization();
        } else {
            error("A statement should be either start a block (conditional, function, loop), call a function, or " +
                    "initialize a variable or lambda function.");
        }
    }

    private void booleanOpts() {
        if (check(TRUE)) consume(TRUE);
        else if (check(FALSE)) consume(FALSE);
        else error("Expected a boolean but received neither 'true' nor 'false'.");
    }

    private void typing() {
        if (typingArrPending()) {
            typingArr();
        } else if (typingKeywordsPending()) {
            typingKeywords();
        } else {
            error("Expected a valid type but did not receive one.");
        }
    }

    private void typingArr() {
        typingKeywords();
        consume(OSQUARE);
        consume(CSQUARE);
    }

    private void typingKeywords() {  // TODO: Grouped-type enumeration
        if (check(INTEGER)) consume(INTEGER);
        else if (check(STRING)) consume(STRING);
        else if (check(BOOL)) consume(BOOL);
        else if (check(FLOAT)) consume(FLOAT);
        else error("Expected 'int', 'float', 'bool', or 'str'");
    }

    private void variableInitializationAssignment() {
        if (arrSlotAssignPending()) {
            arrSlotAssign();
        } else if (assignmentPending()) {
            assignment();
        } else {
            error("Malformed variable initialization/assignment.");
        }
    }

    private void assignment() {
        if (assignmentPrefixPending()) assignmentPrefix();
        consume(IDENTIFIER);
        consume(ASSIGN);
        expression();
    }

    private void assignmentPrefix() {
        if (check(LOCAL)) consume(LOCAL);
        else typing();
    }

    private void arrSlotAssign() {
        if (typingArrPending()) typingArr();
        arrAccess();
        consume(ASSIGN);
        expression();
    }

    private void arr() {
        consume(OSQUARE);
        if (exprListPending()) exprList();
        consume(CSQUARE);
    }

    private void arrAccess() {
        consume(IDENTIFIER);
        consume(OSQUARE);
        expression();
        consume(CSQUARE);
    }

    private void exprList() {
        expression();
        if (check(COMMA)) exprList();
    }

    private void expression() {
        binaryExpression();
    }

    private void binaryExpression() {
        unaryExpression();
        if (binaryOpPending()) {
            binaryOp();
            binaryExpression();
        }
    }

    private void binaryOp() {
        ArrayList<Types> binOps = new ArrayList<>();
        binOps.addAll(List.of(new Types[]{TIMES, DIVIDE, MINUS, PLUS, AND, OR, EQUALITY_COMP, GREATER_OR_EQUAL_COMP, GREATER_THAN_COMP, LESS_THAN_COMP, LESS_OR_EQUAL_COMP}));
        if (binOps.contains(peek())) consume(peek());
        else error("Expected binary operation but did not receive any.");
    }

    private void unaryExpression() {
        if (frontUnaryExpressionPending()) {
            frontUnaryExpression();
        } else if (terminalExpressionPending()) {
            terminalExpression();
            if (backUnaryOperatorPending()) {
                backUnaryOperator();
            }
        } else {
            error("Expected terminal or terminal with unary operator, but did not receive either.");
        }
    }

    private void frontUnaryExpression() {
        frontUnaryOperator();
        terminalExpression();
    }


    private void frontUnaryOperator() {
        ArrayList<Types> ops = new ArrayList<>();
        ops.addAll(List.of(new Types[]{MINUS, NOT}));
        if (ops.contains(peek())) consume(peek());
        else error("Expected '-' or '!' but did not receive either.");
    }


    private void backUnaryOperator() {
        ArrayList<Types> ops = new ArrayList<>();
        ops.addAll(List.of(new Types[]{MINUS_MINUS, PLUS_PLUS}));
        if (ops.contains(peek())) consume(peek());
        else error("Expected '++' or '--' but did not receive either.");
    }

    private void parenExpression() {
        consume(OPAREN);
        expression();
        consume(CPAREN);
    }

    private void terminalExpression() {
        if (check(IDENTIFIER)) {
            if (checkNext(OPAREN)) functionCall();
            else if (checkNext(OSQUARE)) arrAccess();
            else consume(IDENTIFIER);
        } else if (check(STRING_LIT)) {
            consume(STRING_LIT);
        } else if (check(FLOAT_LIT)) {
            consume(FLOAT_LIT);
        }else if (check(INT_LIT)) {
            consume(INT_LIT);
        } else if (parenExpressionPending()) {
            parenExpression();
        } else if (booleanOptsPending()) {
            booleanOpts();
        } else if (arrPending()) {
            arr();
        } else if (check(NULL)) {
            consume(NULL);
        } else {
            error("Expected some kind of terminal expression, but did not receive.");
        }
    }

    private void block() {
        blockStart();
        blockSpace();
    }

    private void blockStart() {
        consume(COLON);
        consume(NEW_LINE);
    }

    private void blockSpace() {
        consume(QUAD_SPACE);
        statement();
        if (check(NEW_LINE)) {
            consume(NEW_LINE);
            blockSpace();
        }
    }

    private void functionCall() {
        consume(IDENTIFIER);
        consume(OPAREN);
        if (!check(CPAREN)) {
            argList();
        }
        consume(CPAREN);
    }

    private void argList() {
        expression();
        if (check(COMMA)) {
            consume(COMMA);
            argList();
        }
    }

    private void paramList() {
        parameter();
        if (check(COMMA)) {
            consume(COMMA);
            paramList();
        }
    }

    private void parameter() {
        if (typingPending()) {
            typing();
        }
        consume(IDENTIFIER);
    }

    private void returnStatement() {
        consume(RETURN);
        expression();
    }

    private void functionDefinition() {
        consume(FUNC);
        if (typingPending()) {
            typing();
        }
        consume(IDENTIFIER);
        consume(OPAREN);
        if (!check(CPAREN)) {
            paramList();
        }
        consume(CPAREN);
        block();
        consume(QUAD_SPACE);
        returnStatement();
    }

    private void lambdaInitialization() {
        consume(LAMBDA);
        consume(IDENTIFIER);
        consume(ASSIGN);
        consume(OPAREN);
        if (!check(CPAREN)) {
            paramList();
        }
        consume(CPAREN);
        consume(RARROW);
        expression();
    }

    private void loopStatement() {
        if (check(WHILE)) {
            whileLoop();
        } else if (check(FOREACH)) {
            forEachLoop();
        } else {
            error("Expected while or for loop");  // this will *never* happen if program's logic is correct
        }
    }

    private void whileLoop() {
        consume(WHILE);
        expression();
        block();
    }

    private void forEachLoop() {
        consume(FOREACH);
        consume(IDENTIFIER);
        consume(IN);
        iterable();
        block();
    }

    private void iterable() {
        if (arrPending()) {
            arr();
        } else if (check(INT_LIT)) {
            if (checkNext(RANGE)) range();
            else consume(INT_LIT);
        } else {
            error("Expected array, range, or integer but did not receive either.");
        }
    }

    private void range() {
        consume(INT_LIT);
        consume(RANGE);
        consume(INT_LIT);
    }

    private void conditionalStatement() {
        ifBlock();
        while (elifBlockPending()) {
            elifBlock();
        }
        if (elseBlockPending()) {
            elseBlock();
        }
    }

    private void ifBlock() {
        consume(IF);
        expression();
        block();
    }

    private void elifBlock() {
        consume(NEW_LINE);
        consume(ELSE);
        consume(IF);
        expression();
        block();
    }

    private void elseBlock() {
        consume(NEW_LINE);
        consume(ELSE);
        expression();
        block();
    }


    // ------------ Pending Functions ------------

    private boolean statementPending() {
        return progStatementPending();
    }

    private boolean progStatementPending() {
        return functionDefinitionPending() || functionCallPending() || variableInitializationAssignmentPending()
                || conditionalStatementPending() || loopStatementPending() || lambdaInitializationPending();
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
        return (assignmentPrefixPending()) || (check(IDENTIFIER) && checkNext(ASSIGN));  // NOTE: this works *only* in the contexts of all usages
    }

    private boolean typingPending() {
        return typingKeywordsPending();
    }

    private boolean arrSlotAssignPending() {
        return check(IDENTIFIER) && checkNext(OSQUARE) && !checkNextNext(CSQUARE);  // NOTE: this works *only* in the contexts of all usages
    }

    private boolean assignmentPrefixPending() {
        return typingPending() || check(LOCAL);
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

    private boolean backUnaryOperatorPending() {
        return check(MINUS_MINUS) || check(PLUS_PLUS);
    }

    private boolean terminalExpressionPending() {
        return check(IDENTIFIER) || check(INT_LIT) || check(FLOAT_LIT) || check(STRING_LIT) || check(TRUE)
                || check(FALSE) || check(OPAREN) || check(NULL);
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


    // ------------ Grouped-Type Enumeration ------------


    // ------------ Error Reporting ------------
    private Lexeme error(String message) {
        Readable.syntaxError(message, currentLexeme);
        return new Lexeme(ERROR, currentLexeme.getLine(), message);
    }

    // ------------ Debugging ------------
}
