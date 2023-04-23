package Readable.Environments;

import Readable.LexicalAnalysis.Lexeme;
import Readable.LexicalAnalysis.Types;

import java.util.*;

import Readable.Evaluating.Library.BuiltInInterface;
import Readable.Evaluating.Library.BuiltIns;
import Readable.Readable;

public class Environment {
    // ------------ Static Variables ------------
    private static final Map<String, Lexeme> builtIns = new HashMap<>();
    static {
        BuiltInInterface c = (args, line, env) -> (BuiltIns.len(args, line, env)); builtIns.put("len", new Lexeme(Types.BUILT_IN_FUNC, -1, c));
        c = (args, line, env) -> (BuiltIns.type(args, line, env)); builtIns.put("type", new Lexeme(Types.BUILT_IN_FUNC, -1, c));
        c = (args, line, env) -> (BuiltIns.print(args, line, env)); builtIns.put("print", new Lexeme(Types.BUILT_IN_FUNC, -1, c));
        c = (args, line, env) -> (BuiltIns.sum(args, line, env)); builtIns.put("sum", new Lexeme(Types.BUILT_IN_FUNC, -1, c));
        c = (args, line, env) -> (BuiltIns.divide(args, line, env)); builtIns.put("divide", new Lexeme(Types.BUILT_IN_FUNC, -1, c));
        c = (args, line, env) -> (BuiltIns.multiply(args, line, env)); builtIns.put("multiply", new Lexeme(Types.BUILT_IN_FUNC, -1, c));
        c = (args, line, env) -> (BuiltIns.subtract(args, line, env)); builtIns.put("subtract", new Lexeme(Types.BUILT_IN_FUNC, -1, c));
        c = (args, line, env) -> (BuiltIns.AND(args, line, env)); builtIns.put("AND", new Lexeme(Types.BUILT_IN_FUNC, -1, c));
        c = (args, line, env) -> (BuiltIns.OR(args, line, env)); builtIns.put("OR", new Lexeme(Types.BUILT_IN_FUNC, -1, c));
        c = (args, line, env) -> (BuiltIns.NOT(args, line, env)); builtIns.put("NOT", new Lexeme(Types.BUILT_IN_FUNC, -1, c));
        c = (args, line, env) -> (BuiltIns.truthy(args, line, env)); builtIns.put("truthy", new Lexeme(Types.BUILT_IN_FUNC, -1, c));
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
            error("'" + identifier.getStringValue() + "' is undefined.", identifier.getLine());
        return value;
    }

    private void unrestrainedAdd(Lexeme identifier, Lexeme value) {
        entries.add(new NamedValue(identifier));
        if (value != null) update(identifier, value);
    }

    public void add(Lexeme identifier, Lexeme value) {
        if (scaleLookup(identifier) != null) {
            error("A variable with name '" + identifier.getStringValue() + "' is already defined and cannot be " +
                    "re-declared.", identifier.getLine());
        } else {
           unrestrainedAdd(identifier, value);
        }
    }

    public void localAdd(Lexeme identifier, Lexeme value) {
        if (softLookup(identifier) != null) {
            error("A variable with name '" + identifier.getStringValue() + "' is already defined and cannot be " +
                    "re-declared.", identifier.getLine());
        } else {
            unrestrainedAdd(identifier, value);
        }
    }

    public void add(Lexeme identifier) {
        add(identifier, null);
    }

    public void update(Lexeme identifier, Lexeme newValue) {
        lookup(identifier);  // raises error if undefined
        for (NamedValue namedValue : entries) {
            if (namedValue.getName().getStringValue().equals(identifier.getStringValue())) {
                namedValue.setValue(newValue);
                return;
            }
        }
        if (parent != null) {
            parent.update(identifier, newValue);
        }
    }

    public void addOrUpdate(Lexeme identifier, Lexeme value) {
        if (scaleLookup(identifier) != null) {
            update(identifier, value);
        } else {
            unrestrainedAdd(identifier, value);
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

    public Environment copy() {
        Environment newEnv = new Environment(this.parent);
        for (NamedValue v : seeEntries()) {
            newEnv.add(v.getName().copy(), v.getValue().copy());
        }
        return newEnv;
    }

    // ------------ Error Reporting ------------
    private void error(String message, int line) {
        Readable.runtimeError(message, line);
    }

}
