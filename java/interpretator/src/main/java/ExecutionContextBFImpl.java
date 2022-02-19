/** A convinient class to contain objects
 * for implementation of the BrainFuck scripts commands.
 * Contains required 30000 bytes-array, ptr, etc.
 @author aiwannafly
 @version 1.0
 */
public class ExecutionContextBFImpl implements ExecutionContextBF {
    ExecutionContextBFImpl(String program) {
        this.program = program;
    }

    /** Changes current cell to the next one
     @throws IndexOutOfBoundsException - the size is const
     */
    @Override
    public void incCodePtr() {
        codePtr++;
        if (codePtr >= CODE_SIZE) {
            throw new IndexOutOfBoundsException();
        }
    }

    /** Changes current cell to the previous one
     @throws IndexOutOfBoundsException - the size is const
     */
    @Override
    public void decCodePtr() {
        codePtr--;
        if (codePtr < 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public void incByte() {
        code[codePtr]++;
    }

    @Override
    public void decByte() {
        code[codePtr]--;
    }

    @Override
    public void incProgramPtr() {
        programPtr++;
    }

    @Override
    public void decProgramPtr() {
        programPtr--;
    }

    @Override
    public int getProgramPtr() {
        return programPtr;
    }

    @Override
    public String getProgram() {
        return program;
    }

    @Override
    public char getByte() {
        return code[codePtr];
    }

    @Override
    public void setByte(char symbol) {
        code[codePtr] = symbol;
    }

    /** Changes current cell to the previous one
     @return The current command of the program or null in
     case if the program ptr is out of bounds
     */
    @Override
    public Character getNextCommandCode() {
        if (programPtr < 0 || programPtr >= program.length()) {
            return null;
        }
        return program.charAt(programPtr);
    }

    private char[] code = new char[CODE_SIZE];
    private int codePtr = 0;
    private int programPtr = 0;
    private String program = null;
}
