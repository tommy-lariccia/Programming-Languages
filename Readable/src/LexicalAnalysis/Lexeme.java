// Written by Tommy Lariccia at the Westminster Schools
// Code instruction provided by Mr (Mitchell) Griest and Mr (Jonathan) Lusth.

package src.LexicalAnalysis;

import java.util.ArrayList;

public class Lexeme {
    // ------------ Instance Variables ------------
    // All lexemes have both of these initialized:
    private Types type;
    private int lineNumber;

    // Type-Value Lexemes (e.g. of type NUMBER) have one of these
    private int integerValue;
    private double decValue;
    private String stringValue;
    private ArrayList<Lexeme> arrVal;


    // ------------ Constructors ------------

    public void main_consc(Types tokenType, int line) {
        type = tokenType;
        lineNumber = line;
    }

    public Lexeme(Types tokenType, int line) {main_consc(tokenType, line);}

    public Lexeme(Types tokenType, int line, int intVal) {
        integerValue = intVal;
        main_consc(tokenType, line);
    }

    public Lexeme(Types tokenType, int line, double decVal) {
        decValue = decVal;
        main_consc(tokenType, line);
    }

    public Lexeme(Types tokenType, int line, String strVal) {
        stringValue = strVal;
        main_consc(tokenType, line);
    }

    public Lexeme(Types tokenType, int line, ArrayList<Lexeme> arr) {
        arrVal = arr;
        main_consc(tokenType, line);
    }


    // ------------ Values ------------

    private int getInt() { return integerValue; }

    private double getFloat() { return decValue; }

    private String getString() { return stringValue; }

    private ArrayList<Lexeme> getArr() { return arrVal; }

    private Object determineVal() {
        switch (type) {
            case INT_LIT -> {
                return getInt();
            }
            case FLOAT_LIT -> {
                return getFloat();
            }
            case STRING_LIT, IDENTIFIER -> {
                return getString();
            }
            case ARRAY -> {
                return getArr();
            }
            default -> { return null; }
        }
    }


    // ------------ Getters & Setters ------------

    public Object getValue() { return determineVal(); }

    public Types getType() { return type; }

    public int getLine() { return lineNumber; }


    // ------------ toString ------------

    public String toString() {
        Object value = getValue();
        if (value != null) {
            return getType().toString() + " Lexene with value " + value + " at line " + getLine() + ".";
        }
        return getType().toString() + " Lexene at line " + getLine() + ".";
    }

}
