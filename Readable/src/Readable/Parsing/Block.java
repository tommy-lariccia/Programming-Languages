package Readable.Parsing;

import Readable.LexicalAnalysis.Lexeme;
import Readable.LexicalAnalysis.Types;

public class Block {
    private int vLine;
    private Lexeme blockLex;
    private Lexeme statementList;

    public Block(Lexeme lex, int pl) {
        blockLex = lex;
        vLine = pl;
        statementList = new Lexeme(Types.STATEMENT_LIST);
        blockLex.addChild(statementList);
    }

    public void addChild(Lexeme child) {
        statementList.addChild(child);
    }

    public int getVLine() {return vLine;}

    public Lexeme getHead() { return blockLex;}
}
