public class CommandStartIteration implements Command {
    @Override
    public void execute(ExecutionContextBF executionContext) {
        char currentByte = executionContext.getByte();
        if (currentByte != 0) {
            executionContext.incProgramPtr();
            return;
        }
        String program = executionContext.getProgram();
        int currentIdx = executionContext.getProgramPtr();
        int bracketsCount = 1; // first bracket is already counted
        for (int i = currentIdx; i < program.length(); i++) {
            if (program.charAt(i) == '[') {
                bracketsCount++;
            } else if (program.charAt(i) == ']') {
                bracketsCount--;
            }
            executionContext.incProgramPtr();
            if (bracketsCount == 0) {
                return;
            }
        }
    }
}
