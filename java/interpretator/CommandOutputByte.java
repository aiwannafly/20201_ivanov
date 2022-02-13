public class CommandOutputByte implements Command {
    @Override
    public void execute(ExecutionContextBF executionContext) {
        char symbol = executionContext.getByte();
        System.out.print(symbol);
        executionContext.incProgramPtr();
    }
}
