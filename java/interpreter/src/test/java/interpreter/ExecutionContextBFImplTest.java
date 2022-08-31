package interpreter;

import org.junit.Test;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

public class ExecutionContextBFImplTest {

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

    @Test
    public void incCodePtr() {
        Exception exception = null;
        try {
            incCodeOverflow();
        } catch (Exception exception1) {
            exception = exception1;
        }
        Assert.assertNotNull(exception);
    }

    @Test
    public void decCodePtr() {
        Exception exception = null;
        executionContext = new ExecutionContextBFImpl(program, configuration);
        try {
            decCodeOverflow();
        } catch (Exception exception1) {
            exception = exception1;
        }
        Assert.assertNotNull(exception);
    }

    @Test
    public void incByte() {
        executionContext = new ExecutionContextBFImpl(program, configuration);
        char b = executionContext.getByte();
        executionContext.incByte();
        Assert.assertEquals(b + 1, executionContext.getByte());
    }

    @Test
    public void decByte() {
        executionContext.incByte();
        char b = executionContext.getByte();
        executionContext.decByte();
        Assert.assertEquals(b - 1, executionContext.getByte());
    }

    @Test
    public void incProgramPtr() {
        int ptr = executionContext.getProgramPtr();
        executionContext.incProgramPtr();
        Assert.assertEquals(executionContext.getNextCommandCode(),
                (Character) program.charAt(ptr + 1));
    }

    @Test
    public void decProgramPtr() {
        executionContext.incProgramPtr();
        int ptr = executionContext.getProgramPtr();
        executionContext.decProgramPtr();
        Assert.assertEquals(executionContext.getNextCommandCode(),
                (Character) program.charAt(ptr - 1));
    }

    @Test
    public void getProgramPtr() {
        int ptr = executionContext.getProgramPtr();
        Assert.assertEquals(executionContext.getNextCommandCode(),
                (Character) program.charAt(ptr));
    }

    @Test
    public void getProgram() {
        Assert.assertEquals(program, executionContext.getProgram());
    }

    @Test
    public void setByte() {
        char b = 'A';
        executionContext.setByte(b);
        Assert.assertEquals(executionContext.getByte(), b);
    }

    @Test
    public void getNextCommandCode() {
        int ptr = executionContext.getProgramPtr();
        Assert.assertEquals(executionContext.getNextCommandCode(),
                (Character) program.charAt(ptr));
    }

    String program = "[->+<]";
    Map<Character, String> configuration = new HashMap<>();
    ExecutionContextBFImpl executionContext = new ExecutionContextBFImpl(program, configuration);
}