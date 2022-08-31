package interpreter;

/** BrainFuck command to increment byte in a current cell
 @author aiwannafly
 @version 1.0
 */
public class CommandIncrementByte implements Command {
    @Override
    public void execute(ExecutionContextBF executionContext) {
        executionContext.incByte();
        executionContext.incProgramPtr();
    }
}
