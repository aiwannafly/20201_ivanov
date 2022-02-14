import junit.framework.TestCase;

public class CommandStartIterationTest extends TestCase {

    public void testExecute() {
        CommandStartIteration cmd = new CommandStartIteration();
        cmd.execute(executionContext);
        assertEquals(5, executionContext.getProgramPtr());
        for (int i = 0; i < 5; i++) {
            executionContext.decProgramPtr();
        }
        executionContext.incByte();
        cmd.execute(executionContext);
        assertEquals(1, executionContext.getProgramPtr());
    }

    String program = "[++-]+++";
    ExecutionContextBF executionContext = new ExecutionContextBFImpl(program);
}