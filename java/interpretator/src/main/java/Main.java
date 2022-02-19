import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;
import org.apache.log4j.Logger;

public class Main {
    private static final Logger log = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("Start program, parse args.");
        parseArgs(args);
        InputStream inputStream = System.in;
        if (null != inputFileName) {
            inputStream = ClassLoader.getSystemResourceAsStream(inputFileName);
            if (null == inputStream) {
                System.err.println("Could not open input file: " + inputFileName
                        + "\n" + USAGE_GUIDE);
                log.error("Input file was not opened.");
                return;
            }
        }
        StringBuilder program = new StringBuilder();
        try (Scanner scanner = new Scanner(inputStream)) {
            while (true) {
                if (inputStream == System.in) {
                    if (!program.toString().isEmpty()) {
                        break; // to avoid endless input from a console
                    }
                }
                try {
                    program.append(scanner.next());
                } catch (NoSuchElementException exception) {
                    log.info("Input was read.");
                    break;
                }
            }
        }
        FactoryOfCommands factory = new ReflexiveFactoryOfCommands();
        try {
            factory.setConfigs(configsFileName);
        } catch (FactoryBadConfigs exception) {
            String errorMsg = exception.getMessage();
            System.err.println(errorMsg
                    + "\n" + USAGE_GUIDE);
            log.error(errorMsg);
            return;
        }
        ExecutionContextBF executionContext = new ExecutionContextBFImpl(program.toString());
        log.info("Starting of interpretation commands");
        while (true) {
            Character commandCode = executionContext.getNextCommandCode();
            if (commandCode == null) {
                log.info("Interpretation successfully finished.");
                break;
            }
            Command command = factory.getCommand(commandCode);
            try {
                command.execute(executionContext);
            } catch (Exception exception) {
                String failMsg = "Program command " + commandCode +
                        " failed: " + exception;
                System.err.println(failMsg);
                log.error(failMsg);
                return;
            }
        }
    }

    public static void parseArgs(String[] args) {
        for (String arg : args) {
            if (arg.startsWith(PROGRAM_PREFIX)) {
                inputFileName = arg.substring((PROGRAM_PREFIX + "=").length());
            } else if (arg.startsWith(CONFIGS_PREFIX)) {
                configsFileName = arg.substring((CONFIGS_PREFIX + "=").length());
            }
        }
    }

    private static final String DEFAULT_CONFIGS = "FactoryConfigs.txt";
    private static String inputFileName = null;
    private static String configsFileName = DEFAULT_CONFIGS;
    private static final String PROGRAM_PREFIX = "--program";
    private static final String CONFIGS_PREFIX = "--configs";
    private static final String USAGE_GUIDE = "java Main --configs=<configs file name>" +
            " --program=<program file name>";
}
