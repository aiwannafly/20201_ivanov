package interpreter;

import org.apache.log4j.Logger;

public class InterpreterBrainFuck implements Interpreter {
    private static final Logger log = Logger.getLogger(InterpreterBrainFuck.class);

    /** Constructor of an interpreter
     @param script - string with commands to execute
     @param configsName - name of a file for factory configs
     */
    public InterpreterBrainFuck(String script, String configsName) {
        program = script;
        configsFileName = configsName;
    }

    /** Executes commands in the script
     @throws ScriptException in case if some commands in scripts have failed
     */
    @Override
    public void runScript() throws ScriptException {
        FactoryOfCommands factory = new ReflexiveFactoryOfCommands();
        try {
            factory.setConfigs(configsFileName);
        } catch (FactoryBadConfigsException exception) {
            String errorMsg = exception.getMessage();
            log.error(errorMsg);
            throw new ScriptException(errorMsg);
        }
        ExecutionContextBF executionContext = new ExecutionContextBFImpl(program,
                factory.getConfigs());
        log.info("Starting of interpretation commands");
        while (true) {
            Character commandCode = executionContext.getNextCommandCode();
            if (commandCode == null) {
                log.info("Interpretation successfully finished.");
                break;
            }
            Command command = null;
            try {
                command = factory.getCommand(commandCode);
            } catch (FactoryFailureException exception) {
                String failMsg = "Program command " + commandCode +
                        " was not found in factory.";
                log.error(failMsg);
                throw new ScriptException(failMsg);
            }
            try {
                command.execute(executionContext);
            } catch (Exception exception) {
                String failMsg = "Program command " + commandCode +
                        " failed: " + exception;
                log.error(failMsg);
                throw new ScriptException(failMsg);
            }
        }
    }

    private String program = null;
    private String configsFileName = null;
}
