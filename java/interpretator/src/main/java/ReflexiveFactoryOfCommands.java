import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

public class ReflexiveFactoryOfCommands implements FactoryOfCommands {
    private static final Logger log = Logger.getLogger(ReflexiveFactoryOfCommands.class);

    @Override
    public Command getCommand(Character code) {
        String commandName = configuration.get(code);
        if (null == commandName) {
            log.error("Command " + code + " was not found " +
                    "in the configuration.");
            return null;
        }
        return getByName(commandName);
    }

    @Override
    public boolean setConfigs(String configsFileName) {
        configuration = new HashMap<>();
        InputStream inputStream = ClassLoader.getSystemResourceAsStream(configsFileName);
        if (inputStream == null) {
            log.error("Could not open input stream for configs.");
            return false;
        }
        Properties property = new Properties();
        try {
            property.load(inputStream);
        } catch (IOException exception) {
            log.error("Configs file had wrong format and was not parsed" +
                    " by Properties class.");
            return false;
        }
        Set<?> codes = property.keySet();
        for (Object object : codes) {
            String stringCode = (String) object;
            if (stringCode.length() > 1) {
                log.error("Command " + stringCode + " was not added to configs, " +
                        "it is not a single char command.");
                return false;
            }
            Character code = stringCode.charAt(0);
            String commandName = property.getProperty(code.toString());
            configuration.put(code, commandName);
        }
        log.info("Configs were successfully set.");
        return true;
    }

    private Command getByName(String name) {
        Class<?> namedClass = null;
        try {
            namedClass = Class.forName(name);
        } catch (ClassNotFoundException exception) {
            log.error("Could not find the class with name: " + name);
            return null;
        }
        Command command = null;
        try {
            command = (Command) namedClass.getDeclaredConstructor().newInstance();
        } catch (Exception exception) {
            log.error("Could not make an instance of a class " + name);
            return null;
        }
        return command;
    }

    private Map<Character, String> configuration;
}
