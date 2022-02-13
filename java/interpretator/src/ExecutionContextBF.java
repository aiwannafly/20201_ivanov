/*
 * Used for commands of Brainfuck programs.
 * https://en.wikipedia.org/wiki/Brainfuck
 */
public interface ExecutionContextBF {

    void incCodePtr();

    void decCodePtr();

    void incByte();

    void decByte();

    void incProgramPtr();

    void decProgramPtr();

    int getProgramPtr();

    String getProgram();

    char getByte();

    void setByte(char symbol);

    Character getNextCommandCode();

    int CODE_SIZE = 30000;
}
