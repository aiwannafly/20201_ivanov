/** BrainFuck command to switch to end iteration / start it again
 @author aiwannafly
 @version 1.0
 */
public class CommandEndIteration implements Command {
    @Override
    public void execute(ExecutionContextBF executionContext) {
        char currentByte = executionContext.getByte();
        if (currentByte == 0) {
            executionContext.incProgramPtr();
            return;
        }
        String program = executionContext.getProgram();
        int currentIdx = executionContext.getProgramPtr();
        int bracketsCount = 0;
        while (currentIdx >= 0) {
            if (program.charAt(currentIdx) == '[') {
                bracketsCount++;
            } else if (program.charAt(currentIdx) == ']') {
                bracketsCount--;
            }
            if (bracketsCount == 0) {
                executionContext.incProgramPtr();
                return;
            }
            executionContext.decProgramPtr();
            currentIdx--;
        }
    }
}
