package com.games.starwars.model.factory;

import java.util.Map;

/** Factory which is supposed to generate
 * objects of StarShip class from their 1-symbol names
 @author aiwannafly
 @version 1.0
 */
public interface FactoryOfObjects {

    Object getObject(Character code) throws FactoryFailureException;

    void setConfigs(String configsFileName) throws FactoryBadConfigsException;

    Map<Character, String> getConfigs();
}
