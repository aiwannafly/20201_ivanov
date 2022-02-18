/** A convinient interface to contain objects
 * for implementation of the BrainFuck scripts commands.
 * Contains required 30000 bytes-array, ptr, etc.
 @author aiwannafly
 @version 1.0
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
