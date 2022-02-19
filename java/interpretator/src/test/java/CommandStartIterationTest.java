import org.junit.Test;
import org.junit.Assert;

public class CommandStartIterationTest {

    @Test
    public void execute() {
        CommandStartIteration cmd = new CommandStartIteration();
        cmd.execute(executionContext);
        Assert.assertEquals(5, executionContext.getProgramPtr());
        for (int i = 0; i < 5; i++) {
            executionContext.decProgramPtr();
        }
        executionContext.incByte();
        cmd.execute(executionContext);
        Assert.assertEquals(1, executionContext.getProgramPtr());
    }

    String program = "[++-]+++";
    ExecutionContextBF executionContext = new ExecutionContextBFImpl(program);
}