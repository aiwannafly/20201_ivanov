package com.games.starwars.factory;

public class FactoryImpl<Type> extends ReflexiveFactoryOfObjects implements Factory<Type> {

    @Override
    public Type getObject(Character code) throws FactoryFailureException {
        try {
            Object o = super.getObject(code);
            return (Type) o;
        } catch (ClassCastException exception) {
            throw new FactoryFailureException("Cast exception: " + exception.getMessage());
        }
    }
}
