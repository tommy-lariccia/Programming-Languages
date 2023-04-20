package Readable.Parsing;

import Readable.LexicalAnalysis.Lexeme;
import Readable.LexicalAnalysis.Types;

import java.util.ArrayList;

import Readable.Readable;

import javax.lang.model.util.SimpleTypeVisitor14;

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
        return reformCond(getTop().getHead());
    }

    private Lexeme reformCond(Lexeme root) { // Sloppy, yes. But does it work? YES.
        if (root.getType() == STATEMENT_LIST) {
            return reformStatementList(root);
        } else if (root.getChildren().size() > 0) {
            Lexeme newRoot = root.copy();
            for (Lexeme lex : root.getChildren())
                newRoot.addChild(reformCond(lex));
            return newRoot;
        }
        return root;
    }

    private Lexeme reformStatementList(Lexeme root) {  // this is READABLE.
        Lexeme newList = new Lexeme(STATEMENT_LIST, root.getLine());
        for (int i = 0; i < root.getChildren().size(); i++) {
            Lexeme lex = root.getChild(i);
            if (lex.getType() == IF) {
                Lexeme cond = new Lexeme(CONDITIONAL_BLOCK, root.getLine());
                cond.addChild(reformCond(lex));
                newList.addChild(cond);
            } else if (lex.getType() == ELSE_IF) {
                if (newList.getChildren().size() > 0 && newList.getChild(newList.getChildren().size() - 1).getType() == CONDITIONAL_BLOCK &&
                        newList.getChild(newList.getChildren().size() - 1).getChildren().size() > 0
                        && newList.getChild(newList.getChildren().size() - 1).getChild(newList.getChild(newList.getChildren().size() - 1).getChildren().size() - 1).getType() != ELSE) {
                    newList.getChild(newList.getChildren().size() - 1).addChild(reformCond(lex));
                } else {
                    error("ELSE_IF block must follow an ELSE_IF or IF block.", lex);
                }
            } else if (lex.getType() == ELSE) {
                if (newList.getChildren().size() > 0 && newList.getChild(newList.getChildren().size() - 1).getType() == CONDITIONAL_BLOCK &&
                        newList.getChild(newList.getChildren().size() - 1).getChildren().size() > 0
                        && newList.getChild(newList.getChildren().size() - 1).getChild(newList.getChild(newList.getChildren().size() - 1).getChildren().size() - 1).getType() != ELSE) {
                    newList.getChild(newList.getChildren().size() - 1).addChild(reformCond(lex));
                } else {
                    error("ELSE block must follow an ELSE_IF or IF block.", lex);
                }
            } else {
                newList.addChild(reformCond(lex));
            }
        }
        return newList;
    }

    private void popLeftoverBlocks() {
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
        while (getTop().getVLine() - line.getVLine() > 0)
            popBlock();
        addToCurrBlock();
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
