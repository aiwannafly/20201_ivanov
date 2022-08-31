package interpreter;

/** BrainFuck command to switch to the next cell
 @author aiwannafly
 @version 1.0
 */
public class CommandIncrementPointer implements Command {
    @Override
    public void execute(ExecutionContextBF executionContext) {
        executionContext.incCodePtr();
        executionContext.incProgramPtr();
    }
}
