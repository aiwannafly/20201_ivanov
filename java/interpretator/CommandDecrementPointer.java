public class CommandDecrementPointer implements Command {
    @Override
    public void execute(ExecutionContextBF executionContext) {
        executionContext.decCodePtr();
        executionContext.incProgramPtr();
    }
}
