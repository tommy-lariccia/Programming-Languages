package Readable.Environments;

import Readable.LexicalAnalysis.Lexeme;

import java.util.ArrayList;

public interface BuiltInInterface {
    public Lexeme call(ArrayList<Lexeme> args, int line);
}
