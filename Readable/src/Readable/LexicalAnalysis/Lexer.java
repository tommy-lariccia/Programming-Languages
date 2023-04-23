// Written by Tommy Lariccia at the Westminster Schools
// Code instruction provided by Mr (Mitchell) Griest and Dr (Jonathan) Lusth.

package Readable.LexicalAnalysis;

import Readable.Readable;
import static Readable.LexicalAnalysis.Types.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Lexer {
    // ------------ Instance Variables ------------
    private final String source;
    private final ArrayList<Lexeme> lexemes = new ArrayList<>();

    private int currentPosition = 0;
    private int startOfCurrLex = 0;
    private int currLineNumber = 1;


    private static final HashMap<String, Types> closedKeywords = new HashMap<>();

    static {
        // Structural
        closedKeywords.put("return", RETURN);
        closedKeywords.put("func", FUNC);
        closedKeywords.put("while", WHILE);
        closedKeywords.put("foreach", FOREACH);
        closedKeywords.put("in", IN);
        closedKeywords.put("if", IF);
        closedKeywords.put("else", ELSE);
        closedKeywords.put("lambda", LAMBDA);

        // Boolean
        closedKeywords.put("and", AND);
        closedKeywords.put("or", OR);

        // Literals
        closedKeywords.put("true", TRUE);
        closedKeywords.put("false", FALSE);
    }

    // ------------ Constructor ------------

    public Lexer(String sourceCode) {
        source = sourceCode;
    }


    // ------------ Helpers ------------

    private boolean isAtEnd() {
        return currentPosition >= source.length();
    }

    private char peekPrevious() {
        if (currentPosition <= 0) {
            return '\0';
        }
        return source.charAt(currentPosition - 1);
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(currentPosition);
    }

    private char peekNext() {
        if (currentPosition + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(currentPosition + 1);
    }

    private boolean match(char expected) {
        if (isAtEnd() || peek() != expected) return false;
        currentPosition++;
        return true;
    }

    private char advance() {
        char curr = peek();
        if (curr == '\n' || curr == '\r') currLineNumber++;
        currentPosition++;
        return curr;  // old character
    }


    // ------------ Character Classification ------------

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }


    // ------------ Lexing ------------

    public ArrayList<Lexeme> lex() {
        while (!isAtEnd()) {
            startOfCurrLex = currentPosition;
            Lexeme nextLex = getNextLexeme();
            if (nextLex != null) lexemes.add(nextLex);
        }
        lexemes.add(new Lexeme(EOF, currLineNumber));
        return lexemes;
    }

    private Lexeme getNextLexeme() {
        char c = advance();

        switch (c) {
            // ignore tabs
            case '\t':
                return null;

            // single-character tokens
            case ':':
                return new Lexeme(COLON, currLineNumber);
            case '-':
                return new Lexeme(MINUS, currLineNumber);
            case '+':
                return new Lexeme(PLUS, currLineNumber);
            case '[':
                return new Lexeme(OSQUARE, currLineNumber);
            case ']':
                return new Lexeme(CSQUARE, currLineNumber);
            case '(':
                return new Lexeme(OPAREN, currLineNumber);
            case ')':
                return new Lexeme(CPAREN, currLineNumber);
            case '*':
                return new Lexeme(TIMES, currLineNumber);
            case ',':
                return new Lexeme(COMMA, currLineNumber);
            case '\n':
            case '\r':
                return new Lexeme(NEW_LINE, currLineNumber);

            // Strictly Two Character Token

            case '.':
                if (match('.')) return new Lexeme(RANGE, currLineNumber);
                else error("Missing Second '.' to Form Range Operator");


                // One or Two Character Tokens

            case '>':
                return new Lexeme(match('=') ? GREATER_OR_EQUAL_COMP : GREATER_THAN_COMP, currLineNumber);
            case '<':
                return new Lexeme(match('=') ? LESS_OR_EQUAL_COMP : LESS_THAN_COMP, currLineNumber);
            case '!':
                return new Lexeme(match('=') ? NOT_EQUAL_COMP : NOT, currLineNumber);
            case '=':
                if (match('>')) return new Lexeme(RARROW, currLineNumber);
                if (match('=')) return new Lexeme(EQUALITY_COMP, currLineNumber);
                else return new Lexeme(ASSIGN, currLineNumber);
            case '/':
                if (match('/')) return handleSingleLineComment();
                if (match('*')) return handleMultiLineComment();
                return new Lexeme(DIVIDE, currLineNumber);

            // Literals, Keywords, Identifiers

            case '"':
                return lexString();
            default:
                if (isDigit(c)) return lexNumber();
                else if (c == ' ') return lexWhitespace();
                else if (isAlpha(c)) return lexIdentifierOrKeyword();
                else error("Unknown Character: " + c);
                return null;
        }
    }

    public Lexeme lexNumber() {
        boolean isInteger = true;
        while (isDigit(peek())) advance();

        if (peek() == '.' && !(peekNext() == '.')) {
            if (!isDigit(peekNext())) error("Malformed Float (Ends in Decimal Point)");
            isInteger = false;
            advance();
            while (isDigit(peek())) advance();
        }

        String numString = source.substring(startOfCurrLex, currentPosition);
        if (isInteger) return new Lexeme(INT_LIT, currLineNumber, Integer.parseInt(numString));
        else return new Lexeme(FLOAT_LIT, currLineNumber, Double.parseDouble(numString));
    }

    public Lexeme lexString() {
        while ((peek() != '"' || peekPrevious() == '\\') && !isAtEnd()) advance();
        String realStr = source.substring(startOfCurrLex + 1, currentPosition);
        if (isAtEnd()) {
            error("Unterminated String (line " + currLineNumber + ")");
            return null;
        } else {
            advance();
        }
        return new Lexeme(STRING_LIT, currLineNumber, realStr);
    }

    public Lexeme lexWhitespace() {
        while (peek() == ' ' && (currentPosition - startOfCurrLex) < 4) advance();
        if ((currentPosition - startOfCurrLex) == 4) return new Lexeme(QUAD_SPACE, currLineNumber);
        else return null;
    }

    public Lexeme lexIdentifierOrKeyword() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(startOfCurrLex, currentPosition);

        Types type = closedKeywords.get(text);

        return (type != null) ? new Lexeme(type, currLineNumber) : new Lexeme(IDENTIFIER, currLineNumber, text);
    }

    public Lexeme handleMultiLineComment() {
        advance();
        while ((peek() != '*' || peekNext() != '/') && !isAtEnd()) {
            advance();
        }
        if (isAtEnd()) {
            error("Unterminated Multi-line Comment (line " + currLineNumber + ")");
            return null;
        }
        advance();
        advance();
        return null;
    }

    public Lexeme handleSingleLineComment() {
        if (isAtEnd()) return null;
        while (!isAtEnd() && peek() != '\n' && peek() != '\r') {
            advance();
        }
        return null;
    }

    // ------------ Error Reporting ------------

    private void error(String message) {
        Readable.syntaxError(message, currLineNumber);
    }

}
