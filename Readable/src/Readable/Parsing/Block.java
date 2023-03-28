package Readable.Parsing;

import Readable.LexicalAnalysis.Lexeme;
import Readable.LexicalAnalysis.Types;

public class Block {
    private int place;
    private Lexeme blockLex;
    private Lexeme statementList;

    public Block(Lexeme lex, int pl) {
        blockLex = lex;
        place = pl;
        statementList = new Lexeme(Types.STATEMENT_LIST);
        blockLex.addChild(statementList);
    }

    public void addChild(Lexeme child) {
        statementList.addChild(child);
    }

    public int getPlace() {return place;}

    public Lexeme getHead() { return blockLex;}
}
