public interface FactoryOfCommands {
    Command getCommand(Character code);

    boolean setConfigs(String configsFileName);
}
