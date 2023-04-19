package Readable.Environments;

import Readable.LexicalAnalysis.Lexeme;
import Readable.LexicalAnalysis.Types;

public class NamedValue {
    // ------------ Instance Variables ------------
    private final Types type;
    private final Lexeme name;

    private Lexeme value;

    // ------------ Constructors ------------
    public NamedValue(Lexeme name, Types type) {
        this.name = name;
        this.type = type;
        this.value = new Lexeme(Types.NULL);
    }

    // ------------ Getters and Setters ------------

    public Types getType() {
        return type;
    }

    public Lexeme getName() {return name;}

    public Lexeme getValue() {return value;}

    public void setValue(Lexeme lex) {value = lex;}

    // ------------ toString ------------

    public String toString() {
        return name.printRepr() + ": " + value.printRepr() + " (" + type + ")";
    }


}
