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
    public Environment() {
        this.parent = null;
        this.entries = new ArrayList<>();
    }

    public Environment(Environment parent) {
        this.parent = parent;
        this.entries = new ArrayList<>();
    }

    // ------------ Core Environment Functions ------------
    private Lexeme softLookup(Lexeme identifier) {
        for (NamedValue namedValue : entries) {
            if (namedValue.getName().getStringValue().equals(identifier.getStringValue())) {
                return namedValue.getValue();
            }
        }
        return null;
    }

    private Lexeme scaleLookup(Lexeme identifier) {
        Lexeme value = softLookup(identifier);
        if (value == null) {
            if (parent != null) return parent.scaleLookup(identifier);
            return null;
        }
        return value;
    }

    public Lexeme lookup(Lexeme identifier) {
        Lexeme value = scaleLookup(identifier);
        if (value == null)
            error("'" + identifier.getValue() + "' is undefined.", identifier.getLine());
        return value;
    }

    public void add(Types type, Lexeme identifier, Lexeme value) {
        if (scaleLookup(identifier) != null) {
            error("A variable with name '" + identifier.getStringValue() + "' is already defined and cannot be " +
                    "re-declared.", identifier.getLine());
        } else {
            localAdd(type, identifier, value);
        }
    }

    public void add(Types type, Lexeme identifier) {
        add(type, identifier, null);
    }

    private void localAdd(Types type, Lexeme identifier, Lexeme value) {
        entries.add(new NamedValue(identifier, type));
        if (value != null) update(null, identifier, value);
    }

    public void update(Types type, Lexeme identifier, Lexeme newValue) {
        lookup(identifier);  // raises error if undefined

        for (NamedValue namedValue : entries) {
            if (namedValue.getName().getStringValue().equals(identifier.getStringValue())) {
                Types declaredType = namedValue.getType();
                Types providedType = newValue.getType();
                if ((declaredType != Types.ANY_TYPE) && type != declaredType && (type != null)) {
                    error("Cannot acknowledge type " + type + " when identifier's type set to " + declaredType, identifier.getLine());
                    return;
                }
                if ((providedType != declaredType) && !(declaredType == Types.ANY_TYPE))
                    newValue = typeElevate(newValue, declaredType);
                if (newValue == null)
                    error("Variable '" + identifier.getValue() + "' has been declared as type " +
                            declaredType + " and cannot be assigned a value of type " + providedType,
                            identifier.getLine());
                namedValue.setValue(newValue);
                return;
            }
        }
        parent.update(type, identifier, newValue);
    }

    public void update(Lexeme identifier, Lexeme newValue) {
        update(null, identifier, newValue);
    }

    private Lexeme typeElevate(Lexeme value, Types type) {
        if (value.getType() == Types.INT_LIT && type == Types.FLOAT_LIT) {
            return new Lexeme(Types.FLOAT_LIT, value.getLine(), (double) value.getIntValue());
        } else if ((value.getType() == Types.INT_LIT || value.getType() == Types.FLOAT_LIT) && type == Types.STRING_LIT) {
            return new Lexeme(Types.STRING_LIT, value.getLine(), String.valueOf(value.getValue()));
        }
        return null;
    }

    public void doInAss(Types type, Lexeme identifier, Lexeme value) {
        if (type == Types.LOCAL) {
            if (softLookup(identifier) != null) {
                error("Identifier " + identifier.getStringValue() + " already" +
                    "used in local scope. Cannot declare.", identifier.getLine()); return;
            }
            localAdd(Types.ANY_TYPE, identifier, value);
        } else if (scaleLookup(identifier) == null) {  // initialization
            add(type, identifier, value);
        } else {
            update(type, identifier, value);
        }
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
