#include<stdio.h>
#include<stdlib.h>
#include<string.h>

#include "pipe_operations.h"

#define COMMAND_BUFFER_SIZE (200)
#define REQUIRED_ARGC (2)
#define FAIL (-1)
#define USAGE_GUIDE "USAGE: prog.exe file_name"

int main(int argc, char *argv[]) {
    if (argc < REQUIRED_ARGC) {
        fprintf(stderr, "%s\n", USAGE_GUIDE);
        return EXIT_FAILURE;
    }
    const char *file_name = argv[1];
    char cat_command[COMMAND_BUFFER_SIZE] = "cat ";
    char tr_command[COMMAND_BUFFER_SIZE] = "tr [:lower:] [:upper:]";
    strcat(cat_command, file_name);
    FILE *cat_pipe = popen(cat_command, "r");
    if (cat_pipe == NULL) {
        fprintf(stderr, "Could not launch cat\n");
        perror("error in popen");
        return EXIT_FAILURE;
    }
    FILE *tr_pipe = popen(tr_command, "w");
    if (tr_pipe == NULL) {
        fprintf(stderr, "Could not launch tr\n");
        perror("error in popen");
        int status = pclose(cat_pipe);
        if (status == FAIL) {
            perror("error in pclose");
        }
        return EXIT_FAILURE;
    }
    char *buffer = fread_from_pipe(cat_pipe);
    if (buffer == NULL) {
        fprintf(stderr, "Error while reading occurred\n");
        goto end;
    }
    int cat_status = pclose(cat_pipe);
    if (cat_status == FAIL) {
        perror("error in pclose");
    } else {
        int exit_status = WEXITSTATUS(cat_status);
        if (exit_status != EXIT_SUCCESS) {
            fprintf(stderr, "program cat failed");
            goto end;
        }
    }
    bool written = fwrite_into_pipe(tr_pipe, buffer, strlen(buffer));
    if (!written) {
        fprintf(stderr, "Error while writing occurred\n");
        goto end;
    }
    end:
    {
        if (NULL != buffer) {
            free(buffer);
        }
        int tr_status = pclose(tr_pipe);
        if (tr_status == FAIL) {
            perror("error in pclose");
        } else {
            int exit_status = WEXITSTATUS(tr_status);
            if (exit_status != EXIT_SUCCESS) {
                fprintf(stderr, "program tr failed");
                goto end;
            }
        }
        return EXIT_SUCCESS;
    }
}
