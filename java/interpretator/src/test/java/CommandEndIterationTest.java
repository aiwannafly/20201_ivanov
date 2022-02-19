import org.junit.Test;
import org.junit.Assert;

public class CommandEndIterationTest {

    @Test
    public void execute() {
        CommandEndIteration cmd = new CommandEndIteration();
        for (int i = 0; i < 4; i++) {
            executionContext.incProgramPtr();
        }
        cmd.execute(executionContext);
        Assert.assertEquals(5, executionContext.getProgramPtr());
        executionContext.decProgramPtr();
        executionContext.incByte();
        cmd.execute(executionContext);
        Assert.assertEquals(1, executionContext.getProgramPtr());
    }

    String program = "[++-]+++";
    ExecutionContextBF executionContext = new ExecutionContextBFImpl(program);
}