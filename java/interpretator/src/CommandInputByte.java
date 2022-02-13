import java.util.Scanner;

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
