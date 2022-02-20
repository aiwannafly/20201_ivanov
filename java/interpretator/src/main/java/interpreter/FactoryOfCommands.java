package interpreter;

import java.util.Map;

/** Factory which is supposed to generate
 * objects of Command class from their 1-symbol names
 @author aiwannafly
 @version 1.0
 */
public interface FactoryOfCommands {
    Command getCommand(Character code) throws FactoryFailureException;

    void setConfigs(String configsFileName) throws FactoryBadConfigsException;

    Map<Character, String> getConfigs();
}
