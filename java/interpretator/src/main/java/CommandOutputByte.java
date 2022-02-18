/** BrainFuck command to output the byte from a current cell
 @author aiwannafly
 @version 1.0
 */
public class CommandOutputByte implements Command {
    @Override
    public void execute(ExecutionContextBF executionContext) {
        char symbol = executionContext.getByte();
        System.out.print(symbol);
        executionContext.incProgramPtr();
    }
}
