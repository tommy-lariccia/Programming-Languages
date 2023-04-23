package Readable.Evaluating.Library;

import Readable.LexicalAnalysis.Lexeme;

import static Readable.LexicalAnalysis.Types.*;

public class Boolean {
    // ----------- Helper -----------
    private static Lexeme helperNegate(Lexeme root) {
        return switch (root.getType()) {
            case TRUE -> new Lexeme(FALSE);
            case FALSE -> new Lexeme(TRUE);
            default -> new Lexeme(NULL);
        };
    }

    // ----------- Truthiness -----------
    public static Lexeme truthy(Lexeme root) {
        Lexeme trueValue = new Lexeme(TRUE);
        Lexeme falseValue = new Lexeme(FALSE);
        return switch (root.getType()) {
            case INT_LIT -> root.getIntValue() != 0 ? trueValue : falseValue;
            case FLOAT_LIT -> root.getDecValue() != 0.0 ? trueValue : falseValue;
            case STRING_LIT -> !(root.getStringValue().equals("")) ? trueValue : falseValue;
            case ARR -> root.getChild(0).getChildren().size() > 0 ? trueValue : falseValue;
            case TRUE -> trueValue;
            case FALSE -> falseValue;
            default -> new Lexeme(NULL);
        };
    }

    // ----------- AND -----------
    public static Lexeme andComp(Lexeme s1, Lexeme s2) {
        return (truthy(s1).getType() == TRUE && truthy(s2).getType() == TRUE) ? new Lexeme(TRUE) : new Lexeme(FALSE);
    }

    // ----------- OR -----------
    public static Lexeme orComp(Lexeme s1, Lexeme s2) {
        return (truthy(s1).getType() == TRUE || truthy(s2).getType() == TRUE) ? new Lexeme(TRUE) : new Lexeme(FALSE);
    }

    // ----------- NOT -----------
    public static Lexeme notComp(Lexeme root) {
        return helperNegate(truthy(root));
    }
}
