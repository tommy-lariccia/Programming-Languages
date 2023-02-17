// Written by Tommy Lariccia at the Westminster Schools
// Code instruction provided by Mr (Mitchell) Griest and Mr (Jonathan) Lusth.

package src.LexicalAnalysis;

import java.util.ArrayList;
import java.util.HashMap;

import static src.LexicalAnalysis.Types.*;

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
        closedKeywords.put("local", LOCAL);

        // Boolean
        closedKeywords.put("and", AND);
        closedKeywords.put("or", OR);

        // Literals
        closedKeywords.put("true", TRUE);
        closedKeywords.put("false", FALSE);

        // Types
        closedKeywords.put("int", INTEGER);
        closedKeywords.put("bool", BOOL);
        closedKeywords.put("float", FLOAT);
        closedKeywords.put("str", STRING);
        closedKeywords.put("null", NULL);
    }

    // ------------ Constructor ------------
    
    public Lexer(String sourceCode) { source = sourceCode; }


    // ------------ Helpers ------------

    private boolean isAtEnd() { return currentPosition >= source.length(); }

    private char peekPrevious() {
        if (currentPosition <= 0) { return '\0'; }
        return source.charAt(currentPosition  - 1);
    }

    private char peek() {
        if (isAtEnd()) { return '\0'; }
        return source.charAt(currentPosition);
    }

    private char peekNext() {
        if (currentPosition + 1 >= source.length()) { return '\0'; }
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

    private boolean isDigit(char c) { return c >= '0' && c <= '9'; }

    private boolean isAlpha(char c) { return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'; }

    private boolean isAlphaNumeric(char c) { return isAlpha(c) || isDigit(c); };


    // ------------ Lexing ------------

    public ArrayList<Lexeme> lex() throws Exception {
        while (!isAtEnd()) {
            startOfCurrLex = currentPosition;
            Lexeme nextLex = getNextLexeme();
            if (nextLex != null) lexemes.add(nextLex);
        }
        lexemes.add(new Lexeme(EOF, currLineNumber));
        return lexemes;
    }

    private Lexeme getNextLexeme() throws Exception {
        char c = advance();

        switch (c) {
            // ignore tabs
            case '\t':
                return null;

            // single-character tokens
            case ':':
                return new Lexeme(COLON, currLineNumber);
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
                else throw new Exception("Missing Second '.' to Form Range Operator");  // TODO: custom erroring


            // One or Two Character Tokens

            case '-':
                return new Lexeme(match('-') ? MINUS_MINUS : MINUS, currLineNumber);
            case '+':
                return new Lexeme(match('+') ? PLUS_PLUS : PLUS, currLineNumber);
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
                else throw new Exception("Unknown Character at line " + currLineNumber);  // TODO: custom erroring
        }
    }

    public Lexeme lexNumber() throws Exception {
        boolean isInteger = true;
        while (isDigit(peek())) advance();

        if (peek() == '.') {
            if (!isDigit(peekNext())) throw new Exception("Malformed Float (Ends in Decimal Point) at Line " + currLineNumber);  // TODO: custom erroring
            isInteger = false;
            advance();
            while (isDigit(peek())) advance();
        }

        String numString = source.substring(startOfCurrLex, currentPosition);
        if (isInteger) return new Lexeme(INTEGER, currLineNumber, Integer.parseInt(numString));
        else return new Lexeme(FLOAT, currLineNumber, Double.parseDouble(numString));
    }

    public Lexeme lexString() {
        while (peek() != '"' || peekPrevious() == '\\') advance();
        String realStr = source.substring(startOfCurrLex, currentPosition);
        return new Lexeme(STRING_LIT, currLineNumber, realStr);
    }

    public Lexeme lexWhitespace() {
        while(peek() == ' ') advance();
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
        while (peek() != '*' || peekNext() != '/') {
            advance();
        }
        advance(); advance();
        return null;
    }

    public Lexeme handleSingleLineComment() {
        advance();
        while (peek() != '\n' && peek() != '\r') {
            advance();
        }
        advance();
        return null;
    }
}
