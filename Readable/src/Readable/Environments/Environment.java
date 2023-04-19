package Readable.Environments;

import Readable.LexicalAnalysis.Lexeme;
import Readable.LexicalAnalysis.Types;

import java.util.*;

import Readable.Readable;

public class Environment {
    // ------------ Static Variables ------------
    private static final Map<String, Lexeme> builtIns = new HashMap<>();
    static {
        BuiltInInterface c = (args, line) -> (BuiltIns.len(args, line)); builtIns.put("len", new Lexeme(Types.BUILT_IN_FUNC, -1, c));
        c = (args, line) -> (BuiltIns.type(args, line)); builtIns.put("type", new Lexeme(Types.BUILT_IN_FUNC, -1, c));
        c = (args, line) -> (BuiltIns.print(args, line)); builtIns.put("print", new Lexeme(Types.BUILT_IN_FUNC, -1, c));
    }

    // ------------ Instance Variables ------------
    private final Environment parent;
    private final ArrayList<NamedValue> entries;

    // ------------ Constructors ------------
    public Environment() {
        this(null);
    }

    public Environment(Environment parent) {
        this.parent = parent;
        this.entries = new ArrayList<>();
    }

    // ------------ Typing ------------

    private Lexeme typeElevate(Lexeme value, Types type) {
        if ((value.getType() == Types.INT_LIT || value.getType() == Types.INTEGER) && (type == Types.FLOAT_LIT || type == Types.FLOAT)) {
            return new Lexeme(Types.FLOAT_LIT, value.getLine(), (double) value.getIntValue());
        } else if ((value.getType() == Types.INT_LIT || value.getType() == Types.INTEGER || value.getType() == Types.FLOAT_LIT || type == Types.FLOAT) && (type == Types.STRING_LIT || type == Types.STRING)) {
            return new Lexeme(Types.STRING_LIT, value.getLine(), String.valueOf(value.getValue()));
        }
        return null;
    }

    private boolean compare(Types first, Types second) {
        return first == second || second == Types.NULL || first == Types.ANY_TYPE || subCompare(first, second)
                || subCompare(second, first);
    }

    private boolean subCompare(Types first, Types second) {
        return (first == Types.BOOL && second == Types.TRUE) || (first == Types.BOOL && second == Types.FALSE) ||
                (first == Types.STRING && second == Types.STRING_LIT) || (first == Types.INTEGER && second == Types.INT_LIT)
                || (first == Types.FLOAT && second == Types.FLOAT_LIT);
    }

    // ------------ Core Environment Functions ------------
    private Lexeme softLookup(Lexeme identifier) {
        for (NamedValue namedValue : entries) {
            if (namedValue.getName().getStringValue().equals(identifier.getStringValue())) {
                return namedValue.getValue();
            }
        }
        if (builtIns.containsKey(identifier.getStringValue()))
            return builtIns.get(identifier.getStringValue());
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

    private void unrestrainedAdd(Types type, Lexeme identifier, Lexeme value) {
        entries.add(new NamedValue(identifier, type));
        if (value != null) update(identifier, value);
    }

    public void add(Types type, Lexeme identifier, Lexeme value) {
        if (scaleLookup(identifier) != null) {
            error("A variable with name '" + identifier.getStringValue() + "' is already defined and cannot be " +
                    "re-declared.", identifier.getLine());
        } else {
           unrestrainedAdd(type, identifier, value);
        }
    }

    public void localAdd(Types type, Lexeme identifier, Lexeme value) {
        if (softLookup(identifier) != null) {
            error("A variable with name '" + identifier.getStringValue() + "' is already defined and cannot be " +
                    "re-declared.", identifier.getLine());
        } else {
            unrestrainedAdd(type, identifier, value);
        }
    }

    public void add(Types type, Lexeme identifier) {
        add(type, identifier, null);
    }

    public void update(Lexeme identifier, Lexeme newValue) {
        lookup(identifier);  // raises error if undefined

        for (NamedValue namedValue : entries) {
            if (namedValue.getName().getStringValue().equals(identifier.getStringValue())) {
                Types declaredType = namedValue.getType();
                Types providedType = newValue.getType();

                if (!compare(declaredType, providedType)) {
                    newValue = typeElevate(newValue, declaredType);
                }

                if (newValue == null)
                    error("Variable '" + identifier.getValue() + "' has been declared as type " +
                                    declaredType + " and cannot be assigned a value of type " + providedType,
                            identifier.getLine());

                namedValue.setValue(newValue);
                return;
            }
        }
        if (parent != null) {
            parent.update(identifier, newValue);
        }
    }

    public void addOrUpdate(Types type, Lexeme identifier, Lexeme value) {
        if (type != Types.ANY_TYPE) {
            add(type, identifier, value);
        } else if (scaleLookup(identifier) != null) {
            update(identifier, value);
        } else {
            unrestrainedAdd(type, identifier, value);
        }
    }

    // ------------ Misc ------------
    public boolean isGlobal() {
        return this.parent == null;
    }

    public ArrayList<NamedValue> seeEntries() {
        return (ArrayList<NamedValue>) entries.clone();
    }

    public String toString() {
        String str = "Environment " + this.hashCode();
        if (parent == null)
            str += "\n    This is the global environment.";
        else
            str += "\n    Parent: " + parent.hashCode();
        str += "\n    Values:";
        str += "\n    ------------";
        for (NamedValue namedValue : entries) {
            str += "\n    " + namedValue.toString();
        }
        for (String builtIn : builtIns.keySet()) {
            str += "\n    " + builtIn + ": [bltIn] (BUILT_IN_FUNC)";
        }
        str += "\n";

        return str;
    }

    // ------------ Error Reporting ------------
    private void error(String message, int line) {
        Readable.runtimeError(message, line);
    }

}
