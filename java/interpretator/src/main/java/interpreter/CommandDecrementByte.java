package interpreter;

/** BrainFuck command to decrement byte in current cell
 @author aiwannafly
 @version 1.0
 */
public class CommandDecrementByte implements Command {
    @Override
    public void execute(ExecutionContextBF executionContext) {
        executionContext.decByte();
        executionContext.incProgramPtr();
    }
}
