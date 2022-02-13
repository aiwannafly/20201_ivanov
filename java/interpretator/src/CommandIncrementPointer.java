public class CommandIncrementPointer implements Command {
    @Override
    public void execute(ExecutionContextBF executionContext) {
        executionContext.incCodePtr();
        executionContext.incProgramPtr();
    }
}
