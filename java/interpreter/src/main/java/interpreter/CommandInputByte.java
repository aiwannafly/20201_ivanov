package interpreter;

import java.util.Scanner;

/** BrainFuck command to insert a byte from the input to the
 * current cell
 @author aiwannafly
 @version 1.0
 */
public class CommandInputByte implements Command {
    @Override
    public void execute(ExecutionContextBF executionContext) {
        Scanner scanner = new Scanner(System.in);
        String symbolString;
        while (true) {
            symbolString = scanner.next();
            if (symbolString.length() != 1) {
                System.out.println("Enter a single symbol.");
            } else {
                break;
            }
        }
        char symbol = symbolString.charAt(0);
        executionContext.setByte(symbol);
        executionContext.incProgramPtr();
    }
}
