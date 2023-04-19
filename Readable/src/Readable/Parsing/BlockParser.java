package Readable.Parsing;

import Readable.LexicalAnalysis.Lexeme;
import Readable.LexicalAnalysis.Types;

import java.util.ArrayList;

import Readable.Readable;

import static Readable.LexicalAnalysis.Types.*;
import static Readable.LexicalAnalysis.Types.ELSE_IF;

public class BlockParser {
    private ArrayList<Block> blockStack = new ArrayList<>();
    private ArrayList<Line> lines;
    private Line line;
    private int place = -1;

    private Line advance() {
        place++;
        if (place >= lines.size())
            line = null;
        else
            line = lines.get(place);
        return line;
    }

    private Block getTop() {
        return blockStack.get(blockStack.size() - 1);
    }

    public BlockParser(ArrayList<Line> lns) {
        lines = lns;
        blockStack.add(new Block(new Lexeme(PROG), 0));
        advance();
    }

    public Lexeme getParsed() {
        while (line != null) {
            handleLine(); advance();
        }
        popLeftoverBlocks();
        return getTop().getHead();
    }

    public void popLeftoverBlocks() {
        while (blockStack.size() > 1) {
            popBlock();
        }
    }

    private void handleLine() {
        if (line.getVLine() == getTop().getVLine()) {
            noVLineChange();
        } else if (line.getVLine() > getTop().getVLine()) {
            rightVLineChange();
        } else {
            leftVLineChange();
        }
    }

    private void noVLineChange() {
        addToCurrBlock();
        optConsumeNewBlock();
    }

    private void leftVLineChange() {
        completeBlock();
        optConsumeNewBlock();
    }

    private void rightVLineChange() {
        error("Cannot jump QUAD_SPACE slots without preceding block statement.", line.getParsed());
    }

    private void addToCurrBlock() {
        getTop().addChild(line.getParsed());
    }

    private void optConsumeNewBlock() {
        if (newBlockPending()) {
            pushBlock();
        }
    }

    private void pushBlock() {
        Block newBlock = new Block(line.getParsed(), getTop().getVLine() + 1);
        blockStack.add(newBlock);
    }

    private boolean newBlockPending() {
        return line.getParsed().getType() == WHILE || line.getParsed().getType() == FOREACH
                || line.getParsed().getType() == FUNC || line.getParsed().getType() == IF
                || line.getParsed().getType() == ELSE_IF || line.getParsed().getType() == ELSE;
    }

    private void completeBlock() {
        handleElifandIfs();
        popBlock();
        addToCurrBlock();
    }

    private void handleElifandIfs() {
        if (getTop().getHead().getType() == ELSE_IF || getTop().getHead().getType() == ELSE) {

        }
    }

    private void popBlock() {
        blockStack.remove(blockStack.size() - 1);
    }

    // ------------ Error Reporting ------------
    private Lexeme error(String message, Lexeme lex) {
        Readable.syntaxError(message, lex);
        return new Lexeme(ERROR, lex.getLine(), message);
    }
}
