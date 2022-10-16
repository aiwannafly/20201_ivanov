#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <wait.h>

#define FAIL (-1)
#define CHILD_FORK_PID (0)

const char *parent_phrase = "        === Фамусов ===\n"
                            "Не я один, все также осуждают.";
const char *parent_reply  = "        === Фамусов ===\n"
                            "(про себя)\n"
                            "Уж втянет он меня в беду.";

int main() {
    char *const program = "cat";
    char *const file_name = "file";
    char *const program_argv[] = {program, file_name, NULL};
    printf("%s\n", parent_phrase);
    int child_pid = fork();
    if (FAIL == child_pid) {
        perror("error in fork");
        return EXIT_FAILURE;
    }
    if (child_pid == CHILD_FORK_PID) { // we are in a child process
        int status = execvp(program, program_argv);
        if (FAIL == status) {
            perror("error in execvp");
            return EXIT_FAILURE;
        }
    } else {
        int status = 0;
        waitpid(child_pid, &status, 0);
        int child_exit_code = WEXITSTATUS(status);
        if (EXIT_FAILURE == child_exit_code) {
            printf("Error in child process occurred.\n");
            return EXIT_FAILURE;
        }
        printf("\n\n\n%s\n\n", parent_reply);
    }
    return EXIT_SUCCESS;
}

// init --> run <--> runnable
//           |
//         sleep

