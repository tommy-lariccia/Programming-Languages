package Readable.Evaluating.Library;

import Readable.LexicalAnalysis.Lexeme;
import Readable.LexicalAnalysis.Types;

import java.util.ArrayList;
import java.util.Collections;

import static Readable.LexicalAnalysis.Types.*;

public class Arithmetic {
    // ----------- Summation -----------
    public static Lexeme sum(Lexeme s1, Lexeme s2) {
        switch (s1.getType()) {
            case INT_LIT -> {
                switch (s2.getType()) {
                    case INT_LIT -> {return new Lexeme(Types.INT_LIT, -1, s1.getIntValue() + s2.getIntValue());}
                    case FLOAT_LIT -> {return new Lexeme(Types.FLOAT_LIT, -1, s1.getIntValue() + s2.getDecValue());}
                    case STRING_LIT -> {return new Lexeme(Types.STRING_LIT, -1, String.valueOf(s1.getIntValue()) + s2.getStringValue());}
                    default -> {return new Lexeme(Types.NULL);}
                }
            }
            case FLOAT_LIT -> {
                switch (s2.getType()) {
                    case INT_LIT -> {return new Lexeme(Types.FLOAT_LIT, -1, s1.getDecValue() + s2.getIntValue());}
                    case FLOAT_LIT -> {return new Lexeme(Types.FLOAT_LIT, -1, s1.getDecValue() + s2.getDecValue());}
                    case STRING_LIT -> {return new Lexeme(Types.STRING_LIT, -1, String.valueOf(s1.getDecValue()) + s2.getStringValue());}
                    default -> {return new Lexeme(Types.NULL);}
                }
            }
            case STRING_LIT -> {
                switch (s2.getType()) {
                    case STRING_LIT -> {return new Lexeme(Types.STRING_LIT, -1, s1.getStringValue() + s2.getStringValue());}
                    case FLOAT_LIT, INT_LIT -> {return new Lexeme(Types.STRING_LIT, -1, s1.getStringValue() + String.valueOf(s2.getValue()));}
                    default -> {return new Lexeme(Types.NULL);}
                }
            }
            case ARR -> {
                if (s2.getType() == ARR) {
                    for (Lexeme child : s2.getChild(0).getChildren())
                        s1.getChild(0).addChild(child);
                } else {
                    s1.getChild(0).addChild(s2);
                }
            }
        }
        return new Lexeme(Types.NULL);
    }

    public static boolean checkSumUnavailable(Lexeme lex) {
        return lex.getType() != STRING_LIT && lex.getType() != INT_LIT &&
                lex.getType() != FLOAT_LIT && lex.getType() != ARR;
    }

    // ----------- Subtraction -----------
    public static Lexeme subtract(Lexeme s1, Lexeme s2) {
        switch (s1.getType()) {
            case INT_LIT -> {
                switch (s1.getType()) {
                    case INT_LIT -> {return new Lexeme(Types.INT_LIT, -1, s1.getIntValue() - s2.getIntValue());}
                    case FLOAT_LIT -> {return new Lexeme(Types.FLOAT_LIT, -1, s1.getIntValue() - s2.getDecValue());}
                }
            }
            case FLOAT_LIT -> {
                switch (s1.getType()) {
                    case INT_LIT -> {return new Lexeme(Types.FLOAT_LIT, -1, s1.getDecValue() - s2.getIntValue());}
                    case FLOAT_LIT -> {return new Lexeme(Types.FLOAT_LIT, -1, s1.getDecValue() - s2.getDecValue());}
                }
            }
        }
        if (s1.getType() == Types.STRING_LIT && s2.getType() == Types.INT_LIT) {
            if (s1.getStringValue().length() > s2.getIntValue())
                return new Lexeme(Types.STRING_LIT, -1, s1.getStringValue().substring(0, s1.getStringValue().length() - s2.getIntValue()));
            else
                return new Lexeme(Types.STRING_LIT, -1, "");
        }
        if (s1.getType() == ARR && s2.getType() == Types.INT_LIT) {
            Lexeme newArr = new Lexeme(ARR);
            if (s1.getChild(0).getChildren().size() > s2.getIntValue()) {
                newArr.addChild(new Lexeme(EXPR_LIST, s1.getLine()));
                for (Lexeme lex : s1.getChild(0).getChildren().subList(0, s1.getChild(0).getChildren().size() - s2.getIntValue()))
                    newArr.getChild(0).addChild(lex);
            } else
                newArr.addChild(new Lexeme(EMPTY_LIST));
            return newArr;
        }
        return new Lexeme(Types.NULL);
    }

    public static boolean checkSubtractionUnavailable(Lexeme lex) {
        return lex.getType() != STRING_LIT && lex.getType() != INT_LIT &&
                lex.getType() != FLOAT_LIT && lex.getType() != ARR;
    }

    // ----------- Multiplication -----------
    public static Lexeme multiply(Lexeme s1, Lexeme s2) {
        Lexeme opt1 = handleMultOneSide(s1, s2);
        if (opt1.getType() != NULL) return opt1;
        Lexeme opt2 = handleMultOneSide(s2, s1);
        return opt2;
    }

    private static Lexeme handleMultOneSide(Lexeme s1, Lexeme s2) {
        if (s1.getType() == INT_LIT && s2.getType() == FLOAT_LIT) {
            return new Lexeme(FLOAT_LIT, s1.getLine(), s1.getIntValue() * s2.getDecValue());
        } else if (s1.getType() == INT_LIT && s2.getType() == INT_LIT) {
            return new Lexeme(INT_LIT, s1.getLine(), s1.getIntValue() * s2.getIntValue());
        } else if (s1.getType() == FLOAT_LIT && s2.getType() == FLOAT_LIT) {
            return new Lexeme(FLOAT_LIT, s1.getLine(), s1.getDecValue() * s2.getDecValue());
        } else if (s1.getType() == INT_LIT && s2.getType() == STRING_LIT) {
            String newString = "";
            String additive = s2.getStringValue();
            if (s1.getIntValue() < 0) {
                additive = new StringBuilder(additive).reverse().toString();
            }
            for (int i = 0; i < Math.abs(s1.getIntValue()); i++) newString += additive;
            return new Lexeme(STRING_LIT, s1.getLine(), newString);
        } else if (s1.getType() == INT_LIT && s2.getType() == ARR) {
            Lexeme newArr = new Lexeme(ARR, s1.getLine());
            Lexeme newList = new Lexeme(EXPR_LIST, s1.getLine());
            ArrayList<Lexeme> arr = (ArrayList<Lexeme>) s1.getChild(0).getChildren().clone();
            if (s2.getIntValue() < 0) {
                Collections.reverse(arr);
            }
            for (int i = 0; i < Math.abs(s1.getIntValue()); i++) newList.getChildren().addAll(arr);
            newArr.addChild(newList);
            return newArr;
        } else if (s1.getType() == FALSE && s2.getType() == INT_LIT) return new Lexeme(INT_LIT, s1.getLine(), 0);
        else if (s1.getType() == FALSE && s2.getType() == FLOAT_LIT) return new Lexeme(FLOAT_LIT, s1.getLine(), 0.0);
        else if (s1.getType() == FALSE && s2.getType() == STRING_LIT) return new Lexeme(STRING_LIT, s1.getLine(), "");
        else if (s1.getType() == FALSE && s2.getType() == ARR) {
            Lexeme newArr = new Lexeme(ARR, s1.getLine());
            newArr.addChild(new Lexeme(EXPR_LIST, s1.getLine()));
            return newArr;
        }
        else if (s1.getType() == TRUE) return s2;
        return new Lexeme(NULL);
    }

    public static boolean checkMultiplicationUnavailable(Lexeme lex) {
        return lex.getType() != STRING_LIT && lex.getType() != INT_LIT &&
                lex.getType() != FLOAT_LIT && lex.getType() != ARR && lex.getType() != TRUE && lex.getType() != FALSE;
    }

    // ----------- Division -----------
    public static Lexeme divide(Lexeme s1, Lexeme s2) {
        if (s1.getType() == INT_LIT && s2.getType() == FLOAT_LIT) {return new Lexeme(FLOAT_LIT, s1.getLine(), s1.getIntValue() / s2.getDecValue());}
        if (s1.getType() == INT_LIT && s2.getType() == INT_LIT) {return new Lexeme(FLOAT_LIT, s1.getLine(), s1.getIntValue() / ((float) s2.getDecValue()));}
        if (s1.getType() == FLOAT_LIT && s2.getType() == INT_LIT) {return new Lexeme(FLOAT_LIT, s1.getLine(), s1.getDecValue() / s2.getIntValue());}
        if (s1.getType() == FLOAT_LIT && s2.getType() == FLOAT_LIT) {return new Lexeme(FLOAT_LIT, s1.getLine(), s1.getDecValue() / s2.getDecValue());}
        return new Lexeme(Types.NULL);
    }

    public static boolean checkDivisionUnavailable(Lexeme lex) {
        return lex.getType() != INT_LIT && lex.getType() != FLOAT_LIT;
    }

}
