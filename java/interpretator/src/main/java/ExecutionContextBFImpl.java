public class ExecutionContextBFImpl implements ExecutionContextBF {
    ExecutionContextBFImpl(String program) {
        this.program = program;
    }

    @Override
    public void incCodePtr() {
        codePtr++;
        if (codePtr >= CODE_SIZE) {
            throw new IndexOutOfBoundsException();
        }
    }

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
