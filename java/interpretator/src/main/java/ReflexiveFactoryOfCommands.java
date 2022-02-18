import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

/** Generates objects from Command class by name
 @author aiwannafly
 @version 1.0
 */
public class ReflexiveFactoryOfCommands implements FactoryOfCommands {
    private static final Logger log = Logger.getLogger(ReflexiveFactoryOfCommands.class);

    /** Returns a command from it's name, which was set in {@link #setConfigs(String)}
     @param code - a character name of a command
     @return command which has name code 
     */
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

    /** Sets configs for factory of commands
     @param configsFileName - a name of a file, which contains factory configs
     @return false if configs were not set, true otherwise
     */
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

    /** Uses java-reflection technology to create Command objects by name
     @param name - name of a class, which implements interface Command
     @return a new instance of the chosen class
     */
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