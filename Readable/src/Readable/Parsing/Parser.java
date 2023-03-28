package Readable.Parsing;

import Readable.LexicalAnalysis.Lexeme;
import Readable.LexicalAnalysis.Types;
import Readable.Readable;

import java.util.ArrayList;
import java.util.List;

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
        buildBlocks();
        return parsed;
    }

    // ------------ Pre-Processing ------------

    private void takeLines() {
        ArrayList<Lexeme> currLine = new ArrayList<>();
        for (Lexeme lex : lexemes) {
            if (lex.getType() == NEW_LINE || lex.getType() == EOF) {
                if (currLine.size() > 0) {currLine.add(new Lexeme(EOL)); lines.add(new Line(currLine));}
                currLine = new ArrayList<>();
            } else {
                currLine.add(lex);
            }
        }
    }

    // ------------ Global Parsing ------------

    private void buildBlocks() {  // TODO: This is a MESS
        ArrayList<Block> blockStack = new ArrayList<>();
        Block top = new Block(new Lexeme(PROG), 0);
        blockStack.add(top);
        for (Line line : lines) {
            if (line.getVLine() == top.getPlace()) {
                top.addChild(line.getParsed());
                if (line.getParsed().getType() == WHILE || line.getParsed().getType() == FOREACH || line.getParsed().getType() == IF || line.getParsed().getType() == FUNC) {
                    Block newBlock = new Block(line.getParsed(), top.getPlace() + 1);
                    blockStack.add(newBlock);
                    top = blockStack.get(blockStack.size() - 1);
                } else if (line.getParsed().getType() == ELSE_IF || line.getParsed().getType() == ELSE) {
                    ArrayList<Lexeme> currLevel = top.getHead().getChildren();
                    if (currLevel.get(currLevel.size() - 1).getType() == IF || currLevel.get(currLevel.size() - 1).getType() == ELSE_IF) {
                        Block newBlock = new Block(line.getParsed(), top.getPlace() + 1);
                        blockStack.add(newBlock);
                        top = blockStack.get(blockStack.size() - 1);
                    } else {
                        error("Cannot have else if or else statement(s) unless if statement above and on same level", line.getParsed());
                    }
                }
            } else if (line.getVLine() > top.getPlace()) {
                error("Cannot jump QUAD_SPACE slots without block statement.", line.getParsed());
            } else {
                blockStack.remove(blockStack.size() - 1);
                top = blockStack.get(blockStack.size() - 1);
                top.addChild(line.getParsed());
                if (line.getParsed().getType() == WHILE || line.getParsed().getType() == FOREACH || line.getParsed().getType() == IF || line.getParsed().getType() == FUNC) {
                    Block newBlock = new Block(line.getParsed(), top.getPlace() + 1);
                    blockStack.add(newBlock);
                    top = blockStack.get(blockStack.size() - 1);
                } else if (line.getParsed().getType() == ELSE_IF || line.getParsed().getType() == ELSE) {
                    ArrayList<Lexeme> currLevel = top.getHead().getChildren();
                    if (currLevel.get(currLevel.size() - 1).getType() == IF || currLevel.get(currLevel.size() - 1).getType() == ELSE_IF) {
                        Block newBlock = new Block(line.getParsed(), top.getPlace() + 1);
                        blockStack.add(newBlock);
                        top = blockStack.get(blockStack.size() - 1);
                    } else {
                        error("Cannot have else if or else statement(s) unless if statement above and on same level", line.getParsed());
                    }
                }
            }
        }
        while (blockStack.size() > 1) {
            blockStack.remove(blockStack.size() - 1);
            top = blockStack.get(blockStack.size() - 1);
        }
        parsed = top.getHead();
    }

    // ------------ Error Reporting ------------
    private Lexeme error(String message, Lexeme lex) {
        Readable.syntaxError(message, lex);
        return new Lexeme(ERROR, lex.getLine(), message);
    }
}
