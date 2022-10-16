#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "linked_list.h"

#define MAX_STR_SIZE 1024
#define STOP_SYMBOL '.'

static void print_string_node(FILE *fp, void *value) {
    char *string = value;
    fprintf(fp, "%s", string);
}

int main() {
    int *a = 4;
    int b = *a;

    errno = 0;
    list_t *list = init_list();
    char *buffer = (char *) malloc(MAX_STR_SIZE * sizeof(*buffer));
    if (NULL == buffer) {
        fprintf(stderr, "%s\n", strerror(errno));
        free_list(list, free);
        return EXIT_FAILURE;
    }
    printf("|-->Enter strings<--|: \n");
    while (true) {
        if (NULL == fgets(buffer, MAX_STR_SIZE, stdin)) {
            fprintf(stderr, "%s\n", strerror(errno));
            free_list(list, free);
            return EXIT_FAILURE;
        }
        if (buffer[0] == STOP_SYMBOL) {
            free(buffer);
            break;
        }
        size_t len = strlen(buffer);
        char *new_string = (char *) malloc((len + 1) * sizeof(*new_string));
        strcpy(new_string, buffer);
        if (!append(list, new_string)) {
            fprintf(stderr, "Could not append a node to a list.\n");
            free_list(list, free);
            return EXIT_FAILURE;
        }
    }
    printf("\n|=>Entered strings<=|:\n");
    print_list(stdout, list, print_string_node);
    free_list(list, free);
    return EXIT_SUCCESS;
}
