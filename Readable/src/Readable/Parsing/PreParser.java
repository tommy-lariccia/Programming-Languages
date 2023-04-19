package Readable.Parsing;

import Readable.LexicalAnalysis.Lexeme;
import Readable.LexicalAnalysis.Types;

import java.util.ArrayList;

public class PreParser {
    // ------------ Instance Variables ------------
    ArrayList<Lexeme> lexemes;

    // ------------ Constructor ------------
    public PreParser(ArrayList<Lexeme> lxms) {
        lexemes = lxms;
    }

    // ------------ Constructor ------------
    public ArrayList<Lexeme> preParse() {
        ArrayList<ArrayList<Lexeme>> lines = makeLines();
        return downsAndUps(lines);
    }

    public ArrayList<ArrayList<Lexeme>> makeLines() {
        ArrayList<ArrayList<Lexeme>> lines = new ArrayList<>();
        ArrayList<Lexeme> currLine = new ArrayList<>();
        for (Lexeme lex : lexemes) {
            if ((lex.getType() == Types.NEW_LINE || lex.getType() == Types.EOF) && currLine.size() > 0) {
                currLine.add(lex);
                lines.add(currLine);
                currLine = new ArrayList<>();
            } else if (lex.getType() != Types.NEW_LINE) {
                currLine.add(lex);
            }
        }
        if (lines.size() > 0)
            lines.get(lines.size() - 1).add(new Lexeme(Types.EOF));
        return lines;
    }

    public ArrayList<Lexeme> downsAndUps(ArrayList<ArrayList<Lexeme>> lines) {
        int pos = 0;
        ArrayList<Lexeme> lexs = new ArrayList<>();
        for (ArrayList<Lexeme> line : lines) {
            int cnt = 0;
            Lexeme lex = line.get(0);
            while (cnt <= pos && lex.getType() == Types.QUAD_SPACE) {
                cnt += 1;
                lex = line.get(cnt);
            }
            if (cnt < pos) {
                for (int i = 0; i < (pos - cnt); i++) {
                    lexs.add(new Lexeme(Types.DOWN));
                    lexs.add(new Lexeme(Types.NEW_LINE));
                }
            } else if (cnt > pos)
                lexs.add(new Lexeme(Types.UP));
            pos = cnt;
            for (int i = cnt; i < line.size(); i++)
                lexs.add(line.get(i));
        }
        return lexs;
    }


}
