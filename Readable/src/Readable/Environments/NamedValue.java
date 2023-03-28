package Readable.Environments;

import Readable.LexicalAnalysis.Lexeme;
import Readable.LexicalAnalysis.Types;

public class NamedValue {
    // ------------ Instance Variables ------------
    private final Types type;
    private final boolean isLocal;
    private final Lexeme name;

    private Lexeme value;

    // ------------ Constructors ------------
    public NamedValue(Lexeme name, Types type, boolean isLocal) {
        this.name = name;
        this.type = type;
        this.isLocal = isLocal;
        this.value = new Lexeme(Types.NULL);
    }

    // ------------ Getters and Setters ------------

    public Types getType() {return type;}

    public boolean isLocal() {return isLocal;}

    public Lexeme getName() {return name;}

    public Lexeme getValue() {return value;}

    public void setValue(Lexeme lex) {value = lex;}

    // ------------ toString ------------

    public String toString() {
        return name.getValue() + ": " + value.toValueOnlyString() + " (" + type + ")";
    }


}
