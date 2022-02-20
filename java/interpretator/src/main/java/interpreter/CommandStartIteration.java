package interpreter;

/** BrainFuck command to start cycle or jump over it
 @author aiwannafly
 @version 1.0
 */
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
        int bracketsCount = 0;
        for (int i = currentIdx; i < program.length(); i++) {
            if (program.charAt(i) == executionContext.getStartIterCode()) {
                bracketsCount++;
            } else if (program.charAt(i) == executionContext.getEndIterCode()) {
                bracketsCount--;
            }
            executionContext.incProgramPtr();
            if (bracketsCount == 0) {
                return;
            }
        }
    }
}
