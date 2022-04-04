package com.games.starwars.factory;

public interface Factory<Type> {

    Type getObject(Character code) throws FactoryFailureException;

    void setConfigs(String configsFileName) throws FactoryBadConfigsException;
}
