#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <wait.h>
#include <unistd.h>

#define FAIL (-1)
#define CHILD_FORK_PID (0)

extern char **environ;

int execvpe(const char *file_name, char *const argv[],
            char *envp[]) {
    environ = envp;
    return execvp(file_name, argv);
}

int main() {
    int child_pid = fork();
    if (FAIL == child_pid) {
        perror("error in fork");
    }
    char *child_environment[] = {"F=ma", NULL};
    char *const program = "env";
    char *const program_argv[] = {program, NULL};
    if (child_pid == CHILD_FORK_PID) {
        int status = execvpe(program, program_argv, child_environment);
        if (FAIL == status) {
            perror("error in execvpe ");
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
