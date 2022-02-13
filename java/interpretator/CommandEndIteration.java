public class CommandEndIteration implements Command {
    @Override
    public void execute(ExecutionContextBF executionContext) {
        char currentByte = executionContext.getByte();
        if (currentByte == 0) {
            executionContext.incProgramPtr();
            return;
        }
        String program = executionContext.getProgram();
        executionContext.decProgramPtr();
        int currentIdx = executionContext.getProgramPtr();
        int bracketsCount = 0;
        while (currentIdx > 0) {
            if (program.charAt(currentIdx) == '[') {
                bracketsCount++;
            } else if (program.charAt(currentIdx) == ']') {
                bracketsCount--;
            }
            executionContext.decProgramPtr();
            if (bracketsCount == 0) {
                executionContext.decProgramPtr();
                executionContext.decProgramPtr();
                executionContext.decProgramPtr();
                return;
            }
            currentIdx--;
        }
    }
}
