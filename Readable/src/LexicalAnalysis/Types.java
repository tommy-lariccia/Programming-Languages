package LexicalAnalysis;

public enum Types {
    // ------------ Structural Characters ------------

    COLON, OSQUARE, CSQUARE, OPAREN, CPAREN,

    // ------------ Data Types ------------

    INTEGER, FLOAT, BOOL, STRING, NULL,

    // ------------ Operators ------------
    PLUS, MINUS, TIMES, DIVIDE,

    EQUALITY_COMP, NOT_EQUAL_COMP, GREATER_THAN_COMP,
    LESS_THAN_COMP, GREATER_OR_EQUAL_COMP,
    LESS_OR_EQUAL_COMP,

    PLUS_PLUS, MINUS_MINUS
}
