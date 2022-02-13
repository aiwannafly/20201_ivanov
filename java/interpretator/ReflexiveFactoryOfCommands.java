import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class ReflexiveFactoryOfCommands implements FactoryOfCommands {
    @Override
    public Command getCommand(Character code) {
        String commandName = configuration.get(code);
        if (null == commandName) {
            return null;
        }
        return getByName(commandName);
    }

    @Override
    public boolean setConfigs(String configsFileName) {
        configuration = new HashMap<>();
        InputStream inputStream = ClassLoader.getSystemResourceAsStream(configsFileName);
        if (inputStream == null) {
            return false;
        }
        Properties property = new Properties();
        try {
            property.load(inputStream);
        } catch (IOException exception) {
            return false;
        }
        Set<?> codes = property.keySet();
        for (Object object : codes) {
            String stringCode = (String) object;
            if (stringCode.length() > 1) {
                return false;
            }
            Character code = stringCode.charAt(0);
            String commandName = property.getProperty(code.toString());
            configuration.put(code, commandName);
        }
        return true;
    }

    private Command getByName(String name) {
        Class<?> namedClass = null;
        try {
            namedClass = Class.forName(name);
        } catch (ClassNotFoundException exception) {
            return null;
        }
        Command command = null;
        try {
            command = (Command) namedClass.getDeclaredConstructor().newInstance();
        } catch (Exception exception) {
            return null;
        }
        return command;
    }

    private Map<Character, String> configuration;
}
