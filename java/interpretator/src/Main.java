import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        parseArgs(args);
        if (null == inputStream) {
            System.out.println("Could not open input file." + "\n" + USAGE_GUIDE);
            return;
        } else if (null == configsFileName) {
            System.out.println("Enter the configs file name with use of --configs="
                    + "\n" + USAGE_GUIDE);
            return;
        }
        Scanner scanner = new Scanner(inputStream);
        StringBuilder program = new StringBuilder();
        while (true) {
            if (inputStream == System.in) {
                if (!program.toString().isEmpty()) {
                    break; // to avoid endless input from a console
                }
            }
            try {
                program.append(scanner.next());
            } catch (NoSuchElementException exception) {
                break;
            }
        }
//        System.out.println(program);
        FactoryOfCommands factory = new ReflexiveFactoryOfCommands();
        if (!factory.setConfigs(configsFileName)) {
            System.out.println("Configs was not opened / has wrong format."
                    + "\n" + USAGE_GUIDE);
            return;
        }
        ExecutionContextBF executionContext = new ExecutionContextBFImpl(program.toString());
        while (true) {
            Character commandCode = executionContext.getNextCommandCode();
            if (commandCode == null) {
                break;
            }
            Command command = factory.getCommand(commandCode);
            try {
                command.execute(executionContext);
            } catch (Exception exception) {
                System.out.println("Program command " + commandCode + " failed: " +
                        exception);
                return;
            }
        }
    }

    public static void parseArgs(String[] args) {
        for (String arg : args) {
            if (arg.startsWith(PROGRAM_PREFIX)) {
                String fileName = arg.substring((PROGRAM_PREFIX + "=").length());
                inputStream = ClassLoader.getSystemResourceAsStream(fileName);
            } else if (arg.startsWith(CONFIGS_PREFIX)) {
                configsFileName = arg.substring((CONFIGS_PREFIX + "=").length());
            }
        }
    }

    private static final String DEFAULT_CONFIGS = "FactoryConfigs.txt";
    private static InputStream inputStream = System.in;
    private static String configsFileName = DEFAULT_CONFIGS;
    private static final String PROGRAM_PREFIX = "--program";
    private static final String CONFIGS_PREFIX = "--configs";
    private static final String USAGE_GUIDE = "java Main --configs=<configs file name> --program=<program file name>";
}
