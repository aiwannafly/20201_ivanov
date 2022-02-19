import org.junit.Test;
import org.junit.Assert;

public class ReflexiveFactoryOfCommandsTest {

    @Test
    public void getCommand() {
        FactoryBadConfigs exception = null;
        try {
            factory.setConfigs(goodFileName);
        } catch (FactoryBadConfigs exception1) {
            exception = exception1;
        }
        Assert.assertNull(exception);
        Assert.assertNull(factory.getCommand('A'));
        Character[] commands = {'+', '-', '>', '<', '[', ']', ',', '.'};
        for (Character command : commands) {
            Assert.assertNotNull(factory.getCommand(command));
        }
    }

    @Test
    public void setConfigs() {
        FactoryBadConfigs exception = null;
        try {
            factory.setConfigs(badFileName);
        } catch (FactoryBadConfigs exception1) {
            exception = exception1;
        }
        Assert.assertNotNull(exception);
        exception = null;
        try {
            factory.setConfigs(awfulFileName);
        } catch (FactoryBadConfigs exception1) {
            exception = exception1;
        }
        Assert.assertNotNull(exception);
        exception = null;
        try {
            factory.setConfigs(goodFileName);
        } catch (FactoryBadConfigs exception1) {
            exception = exception1;
        }
        Assert.assertNull(exception);
    }

    private static final String goodFileName = "FactoryConfigs.txt";
    private static final String badFileName = "HeLLo WoRlD";
    private static final String awfulFileName = "Main.class";
    private static final ReflexiveFactoryOfCommands factory = new ReflexiveFactoryOfCommands();
}