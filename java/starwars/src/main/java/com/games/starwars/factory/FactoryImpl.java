package com.games.starwars.factory;

public class FactoryImpl<Type> extends ReflexiveFactoryOfObjects implements Factory<Type> {

    @Override
    public Type getObject(Character code) throws FactoryFailureException {
        try {
            Object o = super.getObject(code);
            return (Type) o;
        } catch (ClassCastException | FactoryFailureException exception) {
            throw new FactoryFailureException(exception.getMessage());
        }
    }
}
