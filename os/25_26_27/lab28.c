#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <libgen.h>
#include <time.h>

#define FAIL (-1)
#define WRITE_END (0)
#define READ_END (1)
#define NUMS_COUNT (100)

#include "pipe_operations.h"

int p2open(const char *cmd, FILE *fp[2]);
int p2close(FILE *fp[2]);

int main() {
    FILE *pipe_fp[2];
    srand(time(0));
    int return_value = p2open("/bin/sort -n -b", pipe_fp);
    if (return_value == FAIL) {
        fprintf(stderr, "=== Error in p2open, could not launch /bin/sort\n");
        return EXIT_FAILURE;
    }
    for (size_t i = 0; i < NUMS_COUNT; i++) {
        fprintf(pipe_fp[WRITE_END], "%d\n", rand() % NUMS_COUNT);
    }
    fprintf(pipe_fp[WRITE_END], "\n");
    fclose(pipe_fp[WRITE_END]);
    char *result = fread_from_pipe(pipe_fp[READ_END]);
    if (result == NULL) {
        fprintf(stderr, "Could not read from pipe\n");
        return EXIT_FAILURE;
    }
    return_value = p2close(pipe_fp);
    if (return_value == FAIL) {
        free(result);
        fprintf(stderr, "=== Error in p2close occurred\n");
        return EXIT_FAILURE;
    } else if (return_value != EXIT_SUCCESS) {
        free(result);
        fprintf(stderr, "=== Error in sort -n -b occurred\n");
        return EXIT_FAILURE;
    }
    int nums_count = 0;
    char *ptr = result;
    do {
        while(!isdigit(*result) && *result != '\0') {
            result++;
        }
        int num = atoi(result);
        printf("%d ", num);
        nums_count++;
        if (nums_count % 10 == 0) {
            printf("\n");
        }
        if (nums_count == NUMS_COUNT) {
            break;
        }
        while(isdigit(*result) && *result != '\0') {
            result++;
        }
    } while(*result != '\0');
    free(ptr);
    return EXIT_SUCCESS;
}
