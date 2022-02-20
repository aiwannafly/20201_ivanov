import org.junit.Test;
import org.junit.Assert;

public class ReflexiveFactoryOfCommandsTest {

    @Test
    public void getCommand() {
        FactoryBadConfigsException exception = null;
        try {
            factory.setConfigs(goodFileName);
        } catch (FactoryBadConfigsException exception1) {
            exception = exception1;
        }
        Assert.assertNull(exception);
        FactoryFailureException exception1 = null;
        try {
            factory.getCommand('A');
        } catch (FactoryFailureException exception2) {
            exception1 = exception2;
        }
        Assert.assertNotNull(exception1);
        Character[] commands = {'+', '-', '>', '<', '[', ']', ',', '.'};
        for (Character command : commands) {
            exception1 = null;
            try {
                factory.getCommand(command);
            } catch (FactoryFailureException exception2) {
                exception1 = exception2;
            }
            Assert.assertNull(exception1);
        }
    }

    @Test
    public void setConfigs() {
        FactoryBadConfigsException exception = null;
        try {
            factory.setConfigs(badFileName);
        } catch (FactoryBadConfigsException exception1) {
            exception = exception1;
        }
        Assert.assertNotNull(exception);
        exception = null;
        try {
            factory.setConfigs(awfulFileName);
        } catch (FactoryBadConfigsException exception1) {
            exception = exception1;
        }
        Assert.assertNotNull(exception);
        exception = null;
        try {
            factory.setConfigs(goodFileName);
        } catch (FactoryBadConfigsException exception1) {
            exception = exception1;
        }
        Assert.assertNull(exception);
    }

    private static final String goodFileName = "FactoryConfigs.txt";
    private static final String badFileName = "HeLLo WoRlD";
    private static final String awfulFileName = "ReflexiveFactoryOfCommandsTest.class";
    private static final ReflexiveFactoryOfCommands factory = new ReflexiveFactoryOfCommands();
}