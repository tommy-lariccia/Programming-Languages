// Written by Tommy Lariccia at the Westminster Schools
// Code instruction provided by Mr (Mitchell) Griest and Mr (Jonathan) Lusth.

package src.Readable.LexicalAnalysis;

import src.Readable.LexicalAnalysis.Types;

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

    public Lexeme(Types tokenType, int line) {
        type = tokenType;
        lineNumber = line;
    }

    public Lexeme(Types tokenType, int line, int intVal) {
        this(tokenType, line);
        integerValue = intVal;
    }

    public Lexeme(Types tokenType, int line, double decVal) {
        this(tokenType, line);
        decValue = decVal;
    }

    public Lexeme(Types tokenType, int line, String strVal) {
        this(tokenType, line);
        stringValue = strVal;
    }

    public Lexeme(Types tokenType, int line, ArrayList<Lexeme> arr) {
        this(tokenType, line);
        arrVal = arr;
    }


    // ------------ Values ------------

    private int getInt() {
        return integerValue;
    }

    private double getFloat() {
        return decValue;
    }

    private String getString() {
        return stringValue;
    }

    private ArrayList<Lexeme> getArr() {
        return arrVal;
    }

    private Object determineVal() {
        switch (type) {
            case INT_LIT -> {
                return getInt();
            }
            case FLOAT_LIT -> {
                return getFloat();
            }
            case STRING_LIT -> {
                return "'" + getString() + "'";
            }
            case IDENTIFIER -> {
                return "(name: " + getString() + ")";
            }
            case ARRAY -> {
                return getArr();
            }
            default -> {
                return null;
            }
        }
    }


    // ------------ Getters & Setters ------------

    public Object getValue() {
        return determineVal();
    }

    public Types getType() {
        return type;
    }

    public int getLine() {
        return lineNumber;
    }

    public void setLine(int line) {
        lineNumber = line;
    }

    public void setValue(int num) {
        integerValue = num;
    }

    public void setValue(double num) {
        decValue = num;
    }

    public void setValue(String str) {
        stringValue = str;
    }

    public void setValue(ArrayList<Lexeme> arr) {
        arrVal = arr;
    }

    // ------------ toString ------------

    public String toString() {
        Object value = getValue();
        if (value != null) {
            return getType().toString() + ": " + value + " at line " + getLine();
        }
        return getType().toString() + " Lexeme at line " + getLine();
    }

}
