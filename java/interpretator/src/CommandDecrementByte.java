public class CommandDecrementByte implements Command {
    @Override
    public void execute(ExecutionContextBF executionContext) {
        executionContext.decByte();
        executionContext.incProgramPtr();
    }
}
