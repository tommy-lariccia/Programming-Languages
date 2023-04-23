package Readable.Environments;

import Readable.LexicalAnalysis.Lexeme;
import Readable.LexicalAnalysis.Types;

public class NamedValue {
    // ------------ Instance Variables ------------
    private final Lexeme name;
    private Lexeme value;

    // ------------ Constructors ------------
    public NamedValue(Lexeme name) {
        this.name = name;
        this.value = new Lexeme(Types.NULL);
    }

    // ------------ Getters and Setters ------------

    public Lexeme getName() {return name;}

    public Lexeme getValue() {return value;}

    public void setValue(Lexeme lex) {value = lex;}

    // ------------ toString ------------

    public String toString() {
        return name.printRepr() + ": " + value.printRepr();
    }


}
