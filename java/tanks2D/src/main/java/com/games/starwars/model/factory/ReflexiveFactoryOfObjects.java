package com.games.starwars.model.factory;

import com.games.starwars.ApplicationMainClass;
import com.games.starwars.model.ships.StarShip;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/** Generates objects from Command class by name
 @author aiwannafly
 @version 1.0
 */
public class ReflexiveFactoryOfObjects implements FactoryOfObjects {

    /** Returns a command from its name, which was set in {@link #setConfigs(String)}
     @param code - a character name of a command
     @return command which has name code
     */
    @Override
    public Object getObject(Character code) throws FactoryFailureException {
        String className = configuration.get(code);
        if (null == className) {
            String errorMsg = "Command " + code + " was not found " +
                    "in the configuration.";
            throw new FactoryFailureException(errorMsg);
        }
        return getByName(className);
    }

    /** Sets configs for factory of commands
     @param configsFileName - a name of a file, which contains factory configs
     @throws FactoryBadConfigsException if something went wrong
     */
    @Override
    public void setConfigs(String configsFileName) throws FactoryBadConfigsException {
        InputStream inputStream = ApplicationMainClass.class.getResourceAsStream(configsFileName);
        if (inputStream == null) {
            String failMsg = "Could not open input stream for configs.";
            throw new FactoryBadConfigsException(failMsg);
        }

        Properties property = new Properties();
        try {
            property.load(inputStream);
        } catch (IOException exception) {
            String failMsg = "Configs file had wrong format and was not parsed" +
                    " by Properties class.";
            throw new FactoryBadConfigsException(failMsg);
        }
        Set<?> codes = property.keySet();
        for (Object object : codes) {
            String stringCode = (String) object;
            if (stringCode.length() > 1) {
                String failMsg = "Command " + stringCode + " was not added to configs, " +
                        "it is not a single char command.";
                throw new FactoryBadConfigsException(failMsg);
            }
            Character code = stringCode.charAt(0);
            String commandName = property.getProperty(code.toString());
            configuration.put(code, commandName);
        }
    }

    @Override
    public Map<Character, String> getConfigs() {
        return configuration;
    }

    /** Uses java-reflection technology to create Command objects by name
     @param name - name of a class, which implements interface Command
     @return a new instance of the chosen class
     @throws FactoryFailureException if the class was not found,or it didn't get
     a new instance
     */
    private Object getByName(String name) throws FactoryFailureException {
        Class<?> namedClass = null;
        try {
            namedClass = Class.forName(name);
        } catch (ClassNotFoundException exception) {
            String errorMsg = "Could not find the class with name: " + name;
            throw new FactoryFailureException(errorMsg);
        }
        Object object = null;
        try {
            object = namedClass.getDeclaredConstructor().newInstance();
        } catch (Exception exception) {
            String errorMsg = "Could not make an instance of a class " + name;
            System.err.println(exception.getMessage());
            throw new FactoryFailureException(errorMsg);
        }
        return object;
    }

    private final Map<Character, String> configuration = new HashMap<>();
}
