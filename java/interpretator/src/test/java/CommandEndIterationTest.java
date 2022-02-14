import junit.framework.TestCase;

public class CommandEndIterationTest extends TestCase {

    public void testExecute() {
        CommandEndIteration cmd = new CommandEndIteration();
        for (int i = 0; i < 4; i++) {
            executionContext.incProgramPtr();
        }
        cmd.execute(executionContext);
        assertEquals(5, executionContext.getProgramPtr());
        executionContext.decProgramPtr();
        executionContext.incByte();
        cmd.execute(executionContext);
        assertEquals(1, executionContext.getProgramPtr());
    }

    String program = "[++-]+++";
    ExecutionContextBF executionContext = new ExecutionContextBFImpl(program);
}