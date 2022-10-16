#include <ctype.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <sys/types.h>
#include <signal.h>
#include <unistd.h>
#include <wait.h>

#include "pipe_operations.h"

#define CHILD_FORK_PID (0)
#define FAIL (-1)
#define WRITE_END (0)
#define READ_END (1)
#define WARNING_MESSAGE "Warning! The semester ends in one month and you " \
"have passed just 9 labs!\n"

int main() {
    int pipe_fd[2];
    int status = pipe(pipe_fd);
    if (status == FAIL) {
        perror("error in pipe");
        return EXIT_FAILURE;
    }
    pid_t first_child_pid = fork();
    if (first_child_pid == FAIL) {
        perror("error in fork");
        return EXIT_FAILURE;
    }
    pid_t second_child_pid = FAIL;
    if (first_child_pid != CHILD_FORK_PID) { // just parent makes children
        second_child_pid = fork();
        if (second_child_pid == FAIL) {
            perror("error in fork");
            kill(first_child_pid, SIGKILL);
            return EXIT_FAILURE;
        }
    }
    if (first_child_pid == CHILD_FORK_PID) {
        status = close(pipe_fd[WRITE_END]);
        if (status == FAIL) {
            perror("error in close");
        }
        bool written = write_into_pipe(pipe_fd[READ_END], WARNING_MESSAGE, strlen(WARNING_MESSAGE));
        if (!written) {
            perror("error in write");
            status = close(pipe_fd[READ_END]);
            if (status == FAIL) {
                perror("error in close");
            }
            return EXIT_FAILURE;
        }
        status = close(pipe_fd[READ_END]);
        if (status == FAIL) {
            perror("error in close");
        }
        return EXIT_SUCCESS;
    }
    if (second_child_pid == CHILD_FORK_PID) {
        status = close(pipe_fd[READ_END]);
        if (status == FAIL) {
            perror("error in close");
        }
        char *read_buffer = read_from_pipe(pipe_fd[WRITE_END]);
        if (read_buffer == NULL) {
            perror("error in read");
            return EXIT_FAILURE;
        }
        for (size_t i = 0; i < strlen(read_buffer); i++) {
            printf("%c", toupper(read_buffer[i]));
        }
        status = close(pipe_fd[WRITE_END]);
        if (status == FAIL) {
            perror("error in close");
        }
        free(read_buffer);
        return EXIT_SUCCESS;
    }
    status = close(pipe_fd[WRITE_END]);
    if (status == FAIL) {
        perror("error in close");
    }
    status = close(pipe_fd[READ_END]);
    if (status == FAIL) {
        perror("error in close");
    }
    status = 0;
    int wait_status = waitpid(first_child_pid, &status, 0);
    if (wait_status == FAIL) {
        perror("error in waitpid");
    }
    int child_exit_code = WEXITSTATUS(status);
    if (child_exit_code == EXIT_FAILURE) {
        fprintf(stderr, "Error in the first child process occurred.\n");
        kill(second_child_pid, SIGKILL);
        return EXIT_FAILURE;
    }
    wait_status = waitpid(second_child_pid, &status, 0);
    if (wait_status == FAIL) {
        perror("error in waitpid");
    }
    child_exit_code = WEXITSTATUS(status);
    if (child_exit_code == EXIT_FAILURE) {
        fprintf(stderr, "Error in the second child process occurred.\n");
        return EXIT_FAILURE;
    }
    return EXIT_SUCCESS;
}
