#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <wait.h>

#define FAIL (-1)
#define REQUIRED_ARGS_COUNT (2)
#define CHILD_FORK_PID (0)
#define USAGE_GUIDE "USAGE: prog.exe command_name command_args ..."

int main(int argc, char *argv[]) {
    if (argc < REQUIRED_ARGS_COUNT) {
        fprintf(stderr, "Enter name of a command to execute\n%s\n", USAGE_GUIDE);
        return EXIT_FAILURE;
    }
    int child_pid = fork();
    if (FAIL == child_pid) {
        perror("fork");
        return EXIT_FAILURE;
    }
    if (child_pid == CHILD_FORK_PID) { // we are in a child process
        int status = execvp(argv[1], argv + 1);
        if (FAIL == status) {
            perror("execvp");
            return EXIT_FAILURE;
        }
        return EXIT_SUCCESS;
    } else {
        int status = 0;
        waitpid(child_pid, &status, 0);
        int child_exit_code = WEXITSTATUS(status);
        if (EXIT_FAILURE == child_exit_code) {
            printf("Error in child process occurred.\n");
            return EXIT_FAILURE;
        }
        printf("\n=== Child exit code: %d\n", child_exit_code);
        return EXIT_SUCCESS;
    }
}
