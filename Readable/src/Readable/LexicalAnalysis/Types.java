// Written by Tommy Lariccia at the Westminster Schools
// Code instruction provided by Mr (Mitchell) Griest and Mr (Jonathan) Lusth.

package Readable.LexicalAnalysis;

public enum Types {
    // ------------ Structural Characters ------------
    COLON, OSQUARE, CSQUARE, OPAREN, CPAREN, QUAD_SPACE,
    RARROW, NEW_LINE, COMMA,

    // ------------ Operators ------------
    PLUS, MINUS, TIMES, DIVIDE, NEGATE,

    EQUALITY_COMP, NOT_EQUAL_COMP, GREATER_THAN_COMP,
    LESS_THAN_COMP, GREATER_OR_EQUAL_COMP,
    LESS_OR_EQUAL_COMP,

    // ------------ Keywords ------------
    FUNC, RETURN, WHILE, FOREACH, IN, IF, ELSE, LAMBDA,

    // ------------ Boolean ------------
    AND, OR, NOT,

    // ------------ LITERALS ------------
    STRING_LIT, INT_LIT, FLOAT_LIT, TRUE, FALSE,

    // ------------ Misc ------------
    ASSIGN, RANGE, IDENTIFIER, EOF, NULL,

    // ------------ Parse Structures ------------
    ERROR, ELSE_IF, EMPTY_LIST, ANY_TYPE, PARAM, PARAM_LIST, ARB_PARAM_LIST, ARG_LIST, FUNC_CALL, EXPR_LIST, ARR_ACC,
    ARR, ARR_ASS, ASS, TYPING_ARR, STATEMENT_LIST, PROG, EOL, CONDITIONAL_BLOCK, UNPACKABLE,

    // ------------ Interpretive Structures ------------
    BUILT_IN_FUNC
}
