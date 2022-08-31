package interpreter;

/** BrainFuck command to switch to the previous cell
 @author aiwannafly
 @version 1.0
 */
public class CommandDecrementPointer implements Command {
    @Override
    public void execute(ExecutionContextBF executionContext) {
        executionContext.decCodePtr();
        executionContext.incProgramPtr();
    }
}
