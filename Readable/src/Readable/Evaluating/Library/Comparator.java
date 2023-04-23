package Readable.Evaluating.Library;

import Readable.LexicalAnalysis.Lexeme;

import static Readable.LexicalAnalysis.Types.*;

public class Comparator {
    // ----------- Helper -----------
    private static Lexeme helperNegate(Lexeme root) {
        return switch (root.getType()) {
            case TRUE -> new Lexeme(FALSE);
            case FALSE -> new Lexeme(TRUE);
            default -> new Lexeme(NULL);
        };
    }

    // ----------- Greater Than -----------
    public static Lexeme greaterThan(Lexeme s1, Lexeme s2) {
        if (s1.getType() == INT_LIT && s2.getType() == FLOAT_LIT) {
            if (s1.getIntValue() > s2.getDecValue()) return new Lexeme(TRUE);
            else return new Lexeme(FALSE);
        }
        if (s1.getType() == INT_LIT && s2.getType() == INT_LIT) {
            if (s1.getIntValue() > s2.getIntValue()) return new Lexeme(TRUE);
            else return new Lexeme(FALSE);
        }
        if (s1.getType() == FLOAT_LIT && s2.getType() == INT_LIT) {
            if (s1.getDecValue() > s2.getIntValue()) return new Lexeme(TRUE);
            else return new Lexeme(FALSE);
        }
        if (s1.getType() == FLOAT_LIT && s2.getType() == FLOAT_LIT) {
            if (s1.getDecValue() > s2.getDecValue()) return new Lexeme(TRUE);
            else return new Lexeme(FALSE);
        }
        if (s1.getType() == STRING_LIT && s2.getType() == STRING_LIT) {
            if (s1.getStringValue().compareTo(s2.getStringValue()) < 0)
                return new Lexeme(TRUE);
            else return new Lexeme(FALSE);
        }
        return new Lexeme(NULL);
    }

    public static Lexeme greaterThanOrEqualToComp(Lexeme s1, Lexeme s2) {
        return helperNegate(lessThanComp(s1, s2));
    }

    // ----------- Equality -----------
    public static Lexeme equalityComp(Lexeme s1, Lexeme s2) {
        if (s1.getType() == INT_LIT && s2.getType() == FLOAT_LIT) {
            if (s1.getIntValue() == s2.getDecValue()) return new Lexeme(TRUE);
            else return new Lexeme(FALSE);
        }
        if (s1.getType() == INT_LIT && s2.getType() == INT_LIT) {
            if (s1.getIntValue() == s2.getIntValue()) return new Lexeme(TRUE);
            else return new Lexeme(FALSE);
        }
        if (s1.getType() == FLOAT_LIT && s2.getType() == INT_LIT) {
            if (s1.getDecValue() == s2.getIntValue()) return new Lexeme(TRUE);
            else return new Lexeme(FALSE);
        }
        if (s1.getType() == FLOAT_LIT && s2.getType() == FLOAT_LIT) {
            if (s1.getDecValue() == s2.getDecValue()) return new Lexeme(TRUE);
            else return new Lexeme(FALSE);
        } if (s1.getType() == STRING_LIT && s2.getType() == STRING_LIT) {
            if (s1.getStringValue().equals(s2.getStringValue())) return new Lexeme(TRUE);
            else return new Lexeme(FALSE);
        }
        if (s1.getType() == TRUE && s2.getType() == TRUE) {return new Lexeme(TRUE);}
        if (s1.getType() == FALSE && s2.getType() == FALSE) {return new Lexeme(TRUE);}
        if (s1.getType() == TRUE && s2.getType() == FALSE) {return new Lexeme(FALSE);}
        if (s1.getType() == FALSE && s2.getType() == TRUE) {return new Lexeme(FALSE);}
        return new Lexeme(NULL);
    }

    public static Lexeme notEqualComp(Lexeme s1, Lexeme s2) {return helperNegate(equalityComp(s1, s2));}

    // ----------- Less Than -----------

    public static Lexeme lessThanComp(Lexeme s1, Lexeme s2) {
        Lexeme greaterThan = greaterThan(s1, s2);
        Lexeme equalTo = equalityComp(s1, s2);
        return (greaterThan.getType() == FALSE && equalTo.getType() == FALSE) ? new Lexeme(TRUE) : new Lexeme(FALSE);
    }

    public static Lexeme lessThanOrEqualToComp(Lexeme s1, Lexeme s2) {
        return helperNegate(greaterThan(s1, s2));
    }
}
