#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include <unistd.h>

#define COMMAND_BUFFER_SIZE (100)
#define REQUIRED_ARGC (2)
#define FAIL (-1)
#define USAGE_GUIDE "USAGE: prog.exe file_name"

#include "pipe_operations.h"

int main(int argc, char *argv[]) {
    if (argc < REQUIRED_ARGC) {
        fprintf(stderr, "%s\n", USAGE_GUIDE);
        return EXIT_FAILURE;
    }
    const char *file_name = argv[1];
    char pipeline_command[COMMAND_BUFFER_SIZE] = "grep -v '.' ";
    strcat(pipeline_command, file_name);
    strcat(pipeline_command, " | wc -l");
    fprintf(stdout, "Count of empty lines in %s: ", file_name);
    fflush(stdout);
    FILE *pipe_fp = popen(pipeline_command, "r");
    if (pipe_fp == NULL) {
        fprintf(stderr, "Could not launch sh -c (shell)\n");
        perror("error in popen");
        return EXIT_FAILURE;
    }
    char *buffer = fread_from_pipe(pipe_fp);
    if (NULL == buffer) {
        printf("Error while reading occurred\n");
    }
    int pclose_status = pclose(pipe_fp);
    if (pclose_status == FAIL) {
        perror("error in pclose");
    } else {
        int exit_code = WEXITSTATUS(pclose_status);
        if (exit_code != EXIT_SUCCESS) {
            fprintf(stderr, "programs grep, wc failed\n");
            free(buffer);
            return EXIT_FAILURE;
        }
    }
    printf("%s\n", buffer);
    free(buffer);
    return EXIT_SUCCESS;
}
