package interpreter;

import org.junit.Test;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

public class CommandStartIterationTest {

    @Test
    public void execute() {
        Map<Character, String> configuration = new HashMap<>();
        configuration.put('[', "interpreter.CommandStartIteration");
        configuration.put(']', "interpreter.CommandEndIteration");
        String program = "[++-]+++";
        ExecutionContextBF executionContext = new ExecutionContextBFImpl(program, configuration);
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

}