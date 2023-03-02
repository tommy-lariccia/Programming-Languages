package src.Readable.Recognizing;

import src.Readable.LexicalAnalysis.Lexeme;
import src.Readable.LexicalAnalysis.Types;
import src.Readable.Readable;

import java.util.ArrayList;

import static src.Readable.LexicalAnalysis.Types.*;

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

    private Types peakNext() {
        if (nextLexemeIndex >= lexemes.size()) return null;
        return lexemes.get(nextLexemeIndex).getType();
    }

    private boolean check(Types type) {
        return currentLexeme.getType() == type;
    }

    private boolean checkNext(Types type) {
        if (nextLexemeIndex >= lexemes.size()) return false;
        return lexemes.get(nextLexemeIndex).getType() == type;
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
    private void program() {
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
        } else if (functionCallPending()) {
            functionCall();
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
        if (typingKeywordsPending()) {
            typingKeywords();
        } else if (typingArrPending()) {
            typingArr();
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
        if (assignmentPending()) {
            assignment();
        } else if (arrSlotAssignPending()) {
            arrSlotAssign();
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
        consume(LOCAL);
        typing();
    }

    private void arrSlotAssign() {
        if (typingArrPending()) typingArr();
        consume(IDENTIFIER);
        consume(OSQUARE);
        expression();
        consume(CSQUARE);
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
        if () consume(peek());
        else
    }


    // ------------ Pending Functions ------------
    // ------------ Grouped-Type Enumeration ------------
    private void peekIsBinaryOp() {
        ArrayList<Lexeme> binOps; // TODO
    }

    // ------------ Error Reporting ------------
    private Lexeme error(String message) {
        Readable.syntaxError(message, currentLexeme);
        return new Lexeme(ERROR, currentLexeme.getLine(), message);
    }

    // ------------ Debugging ------------
}
