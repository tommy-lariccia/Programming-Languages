package Readable.Environments;

import Readable.LexicalAnalysis.Lexeme;
import Readable.LexicalAnalysis.Types;

import java.util.ArrayList;

import Readable.Readable;

public class Environment {
    // ------------ Instance Variables ------------
    private final Environment parent;
    private final ArrayList<NamedValue> entries;

    // ------------ Constructors ------------
    public Environment(Environment parent) {
        this.parent = parent;
        this.entries = new ArrayList<>();
    }

    // ------------ Core Environment Functions ------------
    private Lexeme softLookup(Lexeme identifier) {
        for (NamedValue namedValue : entries) {
            if (namedValue.getName().equals(identifier)) {
                return namedValue.getValue();
            }
        }
        return null;
    }

    public Lexeme lookup(Lexeme identifier) {
        Lexeme value = softLookup(identifier);
        if (value == null) {
            if (parent != null) return parent.lookup(identifier);
            else {
                error("'" + identifier.getValue() + "' is undefined.", identifier.getLine());
            }
        }
        return value;
    }

    public void add(Types type, Lexeme identifier, Lexeme value, boolean isLocal) {
        if (softLookup(identifier) != null) {
            error("A variable with the name '" + identifier.getValue() + "' is already defined and cannot" +
                    "be re-declared.", value.getLine());
        } else {
            entries.add(new NamedValue(identifier, type, isLocal));
            if (value != null) update(identifier, value);
        }
    }

    public void add(Types type, Lexeme identifier, boolean isLocal) {
        add(type, identifier, null, isLocal);
    }

    public void update(Lexeme identifier, Lexeme newValue) {
        lookup(identifier);  // raises error if undefined

        for (NamedValue namedValue : entries) {
            if (namedValue.getName().equals(identifier)) {
                Types declaredType = namedValue.getType();
                Types providedType = newValue.getType();
                if (providedType != declaredType)
                    newValue = typeElevate(newValue, declaredType);
                if (newValue == null)
                    error("Variable '" + identifier.getValue() + "' has been declared as type " +
                            declaredType + " and cannot be assigned a value of type " + providedType,
                            identifier.getLine());
                namedValue.setValue(newValue);
                return;
            }
        }
        parent.update(identifier, newValue);
    }

    private Lexeme typeElevate(Lexeme value, Types type) {
        if (value.getType() == Types.INT_LIT && type == Types.FLOAT_LIT) {
            return new Lexeme(Types.FLOAT_LIT, value.getLine(), (double) value.getIntValue());
        } else if ((value.getType() == Types.INT_LIT || value.getType() == Types.FLOAT_LIT) && type == Types.STRING_LIT) {
            return new Lexeme(Types.STRING_LIT, value.getLine(), String.valueOf(value.getValue()));
        }
        return null;
    }

    // ------------ toString ------------

    public String toString() {
        String str = "Environment " + this.hashCode();
        if (parent == null)
            str += "\n    This is the global environment.";
        else
            str += "\n    Parent: " + parent.hashCode();
        str += "\n    Values:";
        str += "\n ------------";
        for (NamedValue namedValue : entries) {
            str += "\n    " + namedValue.toString();
        }
        str += "\n";
        return str;
    }


    // ------------ Error Reporting ------------
    private void error(String message, int line) {
        Readable.runtimeError(message, line);
    }
}
