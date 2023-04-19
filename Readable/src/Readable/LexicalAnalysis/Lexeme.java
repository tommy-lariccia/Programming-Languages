// Written by Tommy Lariccia at the Westminster Schools
// Code instruction provided by Mr (Mitchell) Griest and Mr (Jonathan) Lusth.

package Readable.LexicalAnalysis;

import Readable.Environments.BuiltInInterface;
import Readable.Environments.BuiltIns;
import Readable.Environments.Environment;

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

    // Children
    private ArrayList<Lexeme> children = new ArrayList<>();

    // For Closures
    private Environment definingEnv;

    // For Built-Ins
    private BuiltInInterface func;

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

    public Lexeme(Types tokenType, int line, BuiltInInterface callFunc) {
        this(tokenType, line);
        func = callFunc;
    }

    private ArrayList<Lexeme> getArr() {
        return getChild(0).getChildren();
    }

    // ------------ Getters & Setters ------------

    public int getIntValue() {
        return integerValue;
    }

    public double getDecValue() {
        return decValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public Object getValue() {
        switch (type) {
            case INT_LIT -> {
                return getIntValue();
            } case FLOAT_LIT -> {
                return getDecValue();
            } case STRING_LIT -> {
                return getStringValue();
            } case ARR -> {
                return getChild(0).getChildren();
            } case TRUE -> {
                return true;
            } case FALSE -> {
                return false;
            } default -> {
                return null;
            }
        }
    }

    public Types getType() {
        return type;
    }

    public int getLine() {
        if (getType() == Types.NEW_LINE) return lineNumber - 1;
        return lineNumber;
    }

    public Environment getDefiningEnv() {
        return definingEnv;
    }

    public void addChild(Lexeme lex) {
        children.add(lex);
    }

    public void addAllChildren(ArrayList<Lexeme> newChildren) {children.addAll(newChildren);}

    public void setDefiningEnv(Environment newEnv) {definingEnv = newEnv;}

    public Lexeme getChild(int i) {return children.get(i);}

    public ArrayList<Lexeme> getChildren() {return children;}

    public Lexeme copy() {  // a copy without any children
        Lexeme copy = new Lexeme(this.type);
        copy.lineNumber = this.lineNumber;
        copy.integerValue = this.integerValue;
        copy.decValue = this.decValue;
        copy.stringValue = this.stringValue;
        return copy;
    }

    public BuiltInInterface getBuiltInFunc() {return func;}

    // ------------ toString ------------
    private String getRepr() {
        switch (type) {
            case INT_LIT -> {
                return String.valueOf(getIntValue());
            }
            case FLOAT_LIT -> {
                return String.valueOf(getDecValue());
            }
            case STRING_LIT -> {
                return "'" + getStringValue() + "'";
            }
            case IDENTIFIER -> {
                return "(name: " + getStringValue() + ")";
            }
            case ARR -> {
                return String.valueOf(getArr());
            }
            case TRUE -> {
                return String.valueOf(true);
            }
            case FALSE -> {
                return String.valueOf(false);
            }
            default -> {
                return null;
            }
        }
    }

    public String toString() {
        String value = getRepr();
        if (value != null) {
            return getType().toString() + ": " + value + " at line " + getLine();
        }
        return getType().toString() + " Lexeme at line " + getLine();
    }

    public String printRepr() {
        if (getType() == Types.ARR) {
            if (getChild(0).getChildren().size() >= 1) {
                String s = "[" + getChild(0).getChild(0).printRepr();
                for (int i = 1; i < getChild(0).getChildren().size(); i++)
                    s += ", " + getChild(0).getChild(i).printRepr();
                return s + "]";
            } else {
                return "[]";
            }
        } else if (getType() == Types.FUNC) {
            return "[fnct]";
        } else if (getType() == Types.IDENTIFIER) {
            return getStringValue();
        }
        return String.valueOf(getValue());
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
}
