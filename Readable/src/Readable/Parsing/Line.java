package Readable.Parsing;

import Readable.LexicalAnalysis.Lexeme;
import Readable.LexicalAnalysis.Types;

import java.util.ArrayList;

public class Line {
    private int vLine = 0;
    private final ArrayList<Lexeme> lexemes;
    private Lexeme parsed;

    public Line(ArrayList<Lexeme> lexes) {
        lexemes = lexes;
        exciseQuadSpaces();
    }

    public void parse() {
        parsed = new LineParser(lexemes).program();
    }

    public String toString() {
        String str = "LINE (vLine = " + vLine + "):";
        for (Lexeme lex : lexemes) {
            str += "\n    " + lex.toString();
        }
        return str;
    }

    public int getVLine() {return vLine;}

    public Lexeme getParsed() {return parsed;}

    private void exciseQuadSpaces() {
        while (lexemes.get(0).getType() == Types.QUAD_SPACE) {
            vLine++;
            lexemes.remove(0);
        }
    }
}
