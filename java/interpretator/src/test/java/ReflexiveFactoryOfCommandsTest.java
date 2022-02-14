import junit.framework.TestCase;

public class ReflexiveFactoryOfCommandsTest extends TestCase {

    public void testGetCommand() {
        assertTrue(factory.setConfigs(goodFileName));
        assertNull(factory.getCommand('A'));
        Character[] commands = {'+', '-', '>', '<', '[', ']', ',', '.'};
        for (Character command : commands) {
            assertNotNull(factory.getCommand(command));
        }
    }

    public void testSetConfigs() {
        assertFalse(factory.setConfigs(badFileName));
        assertFalse(factory.setConfigs(awfulFileName));
        assertTrue(factory.setConfigs(goodFileName));
    }

    private static final String goodFileName = "FactoryConfigs.txt";
    private static final String badFileName = "HeLLo WoRlD";
    private static final String awfulFileName = "Main.class";
    private static final ReflexiveFactoryOfCommands factory = new ReflexiveFactoryOfCommands();
}