package interpreter;

import org.junit.Test;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

public class CommandEndIterationTest {

    @Test
    public void execute() {
        String program = "[++-]+++";
        Map<Character, String> configuration = new HashMap<>();
        configuration.put('[', "interpreter.CommandStartIteration");
        configuration.put(']', "interpreter.CommandEndIteration");

        ExecutionContextBF executionContext = new ExecutionContextBFImpl(program, configuration);
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
}