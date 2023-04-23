package Readable.Parsing;

import Readable.LexicalAnalysis.Lexeme;
import Readable.Readable;

import java.util.ArrayList;

import static Readable.LexicalAnalysis.Types.*;

public class Parser {
    // ------------ Instance Variables ------------
    private final ArrayList<Lexeme> lexemes;
    private ArrayList<Line> lines = new ArrayList<>();
    private Lexeme parsed;

    // ------------ Constructor ------------
    public Parser(ArrayList<Lexeme> lexemeList) {
        lexemes = lexemeList;
    }

    // ------------ Consumption Functions ------------
    public Lexeme program() {
        takeLines();
        for (Line line : lines) line.parse();
        return (new BlockParser(lines)).getParsed();
    }

    // ------------ Pre-Processing ------------

    private void takeLines() {
        ArrayList<Lexeme> currLine = new ArrayList<>();
        for (Lexeme lex : lexemes) {
            if (lex.getType() == NEW_LINE || lex.getType() == EOF) {
                if (currLine.size() > 0) {currLine.add(new Lexeme(EOL, lex.getLine())); lines.add(new Line(currLine));}
                currLine = new ArrayList<>();
            } else {
                currLine.add(lex);
            }
        }
    }
}
