import junit.framework.TestCase;

public class ExecutionContextBFImplTest extends TestCase {

    private void incCodeOverflow() {
        for (int i = 0; i < ExecutionContextBFImpl.CODE_SIZE; i++) {
            executionContext.incCodePtr();
        }
    }

    private void decCodeOverflow() {
        for (int i = 0; i < ExecutionContextBFImpl.CODE_SIZE; i++) {
            executionContext.decCodePtr();
        }
    }

    public void testIncCodePtr() {
        Exception exception = null;
        try {
            incCodeOverflow();
        } catch (Exception exception1) {
            exception = exception1;
        }
        assertNotNull(exception);
    }

    public void testDecCodePtr() {
        Exception exception = null;
        executionContext = new ExecutionContextBFImpl(program);
        try {
            decCodeOverflow();
        } catch (Exception exception1) {
            exception = exception1;
        }
        assertNotNull(exception);
    }

    public void testIncByte() {
        executionContext = new ExecutionContextBFImpl(program);
        char b = executionContext.getByte();
        executionContext.incByte();
        assertEquals(b + 1, executionContext.getByte());
    }

    public void testDecByte() {
        executionContext.incByte();
        char b = executionContext.getByte();
        executionContext.decByte();
        assertEquals(b - 1, executionContext.getByte());
    }

    public void testIncProgramPtr() {
        int ptr = executionContext.getProgramPtr();
        executionContext.incProgramPtr();
        assertEquals(executionContext.getNextCommandCode(),
                (Character) program.charAt(ptr + 1));
    }

    public void testDecProgramPtr() {
        executionContext.incProgramPtr();
        int ptr = executionContext.getProgramPtr();
        executionContext.decProgramPtr();
        assertEquals(executionContext.getNextCommandCode(),
                (Character) program.charAt(ptr - 1));
    }

    public void testGetProgramPtr() {
        int ptr = executionContext.getProgramPtr();
        assertEquals(executionContext.getNextCommandCode(),
                (Character) program.charAt(ptr));
    }

    public void testGetProgram() {
        assertEquals(program, executionContext.getProgram());
    }

    public void testSetByte() {
        char b = 'A';
        executionContext.setByte(b);
        assertEquals(executionContext.getByte(), b);
    }

    public void testGetNextCommandCode() {
        int ptr = executionContext.getProgramPtr();
        assertEquals(executionContext.getNextCommandCode(),
                (Character) program.charAt(ptr));
    }

    String program = "[->+<]";
    ExecutionContextBFImpl executionContext = new ExecutionContextBFImpl(program);
}