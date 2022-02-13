public class CommandIncrementByte implements Command {
    @Override
    public void execute(ExecutionContextBF executionContext) {
        executionContext.incByte();
        executionContext.incProgramPtr();
    }
}
