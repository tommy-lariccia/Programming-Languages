// Written by Tommy Lariccia at the Westminster Schools
// Code instruction provided by Mr (Mitchell) Griest and Mr (Jonathan) Lusth.

package Readable.LexicalAnalysis;

import java.util.ArrayList;

public class Lexeme {
    // ------------ Instance Variables ------------
    // All lexemes have this initialized:
    private final Types type;
    
    // MOST lexemes have this initialized:
    private int lineNumber;

    // Type-Value Lexemes (e.g. of type NUMBER) have one of these
    private int integerValue;
    private double decValue;
    private String stringValue;
    private ArrayList<Lexeme> arrVal;

    // Children
    private ArrayList<Lexeme> children = new ArrayList<>();


    // ------------ Constructors ------------

    public Lexeme(Types tokenType, int line) {
        type = tokenType;
        lineNumber = line;
    }

    public Lexeme(Types tokenType) {
        type = tokenType;
        lineNumber = -1;
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
        if (getType() == Types.NEW_LINE) return lineNumber - 1;
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

    public void addChild(Lexeme lex) {children.add(lex); simpleElevation(lex);}

    public void addAllChildren(ArrayList<Lexeme> newChildren) {children.addAll(newChildren);}

    public Lexeme getChild(int i) {return children.get(i);}

    public ArrayList<Lexeme> getChildren() {return children;}

    public Lexeme copy() {  // a copy without any children
        Lexeme copy = new Lexeme(this.type);
        copy.lineNumber = this.lineNumber;
        copy.integerValue = this.integerValue;
        copy.decValue = this.decValue;
        copy.stringValue = this.stringValue;
        copy.arrVal = this.arrVal;
        return copy;
    }

    // ------------ toString ------------

    public String toString() {
        Object value = getValue();
        if (value != null) {
            return getType().toString() + ": " + value + " at line " + getLine();
        }
        return getType().toString() + " Lexeme at line " + getLine();
    }

    // --------------- Printing Lexemes as Parse Trees ---------------

    public void printAsParseTree() {
        System.out.println(getPrintableTree(this, 0));
    }

    private static String getPrintableTree(Lexeme root, int level) {
        if (root == null) return "(Empty ParseTree)";
        StringBuilder treeString = new StringBuilder(root.toString());
        StringBuilder spacer = new StringBuilder("\n");
        spacer.append("\t".repeat(level));
        int numChildren = root.children.size();
        if (numChildren > 0) {
            treeString.append(" (with ").append(numChildren).append(numChildren == 1 ? " child):" : " children):");
            for (int i = 0; i < numChildren; i++) {
                Lexeme child = root.getChild(i);
                treeString.append(spacer).append("(").append(i + 1).append(") ").append(getPrintableTree(child, level + 1));
            }
        }
        return treeString.toString();
    }

    // ------------ Misc ------------

    private void simpleElevation(Lexeme justAdded) {
        if ((this.getType() == Types.TIMES || this.getType() == Types.PLUS) && justAdded.getType() == this.getType()) {
            children.remove(children.size() - 1);
            children.addAll(justAdded.getChildren());
        }
        return;
    }
}
