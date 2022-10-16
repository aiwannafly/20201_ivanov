#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#include "linked_list.h"

#define FAIL (-1)
#define REQUIRED_ARGC (2)
#define TIME_OUT (10)
#define TIME_OUT_CODE (0)
#define USAGE_GUIDE "usage: ./prog <file 1> <file 2> ... <file n>"
#define BUF_SIZE (256)

/*
 * Returns a list with file pointers (FILE *).
 * If some errors occur while opening the files from
 * argv, it will indicate about it
 */
static list_t *open_files_with_errors_print(int argc, char *argv[]) {
    size_t files_count = argc - 1;
    list_t *files = init_list();
    for (size_t i = 0; i < files_count; i++) {
        const char *file_name = argv[i + 1];
        FILE *fp = fopen(file_name, "r");
        if (fp == NULL) {
            fprintf(stderr, "=== Error in fopen(%s, 'r')", file_name);
            perror("");
            continue;
        }
        bool appended = append(files, fp);
        if (!appended) {
            fprintf(stderr, "=== Error in append");
        }
    }
    return files;
}

int main(int argc, char *argv[]) {
    if (argc < REQUIRED_ARGC) {
        printf("%s\n", USAGE_GUIDE);
        return EXIT_FAILURE;
    }
    list_t *opened_files = open_files_with_errors_print(argc, argv);
    if (opened_files->len == 0) {
        fprintf(stderr, "=== No files were opened\n");
        free_list(opened_files, NULL);
        return EXIT_FAILURE;
    }
    char *buffer = malloc(BUF_SIZE);
    if (buffer == NULL) {
        perror("=== Error in malloc()");
        free_list(opened_files, NULL);
        return EXIT_FAILURE;
    }
    list_node_t *current_node = opened_files->head;
    while (opened_files->len > 0) {
        fd_set read_set;
        FD_ZERO(&read_set);
        FILE *fp = current_node->value;
        int fd = fileno(fp);
        if (fd == FAIL) {
            perror("=== Error in fileno()");
            break;
        }
        FD_SET(fd, &read_set);
        struct timeval time_out = {
                .tv_sec = TIME_OUT,
                .tv_usec = 0
        };
        int return_value = select(fd + 1, &read_set, NULL, NULL, &time_out);
        if (return_value == FAIL) {
            perror("=== Error in select()");
            break;
        }
        if (return_value == TIME_OUT_CODE) {
            current_node = get_next_node(opened_files, current_node);
            continue;
        }
        assert(FD_ISSET(fd, &read_set));
        size_t capacity = BUF_SIZE;
        ssize_t read_count = getline((char **) &buffer, &capacity, fp);
        if (read_count == 0 || feof(fp) || ferror(fp)) {
            if (ferror(fp)) {
                fprintf(stderr, "=== Error in getline() occurred\n");
            }
            fclose(fp);
            list_node_t *prev_node = current_node;
            current_node = get_next_node(opened_files, current_node);
            push_to_tail(opened_files, prev_node);
            pop_tail(opened_files);
            continue;
        }
        current_node = get_next_node(opened_files, current_node);
        printf("%s", buffer);
    }
    free(buffer);
    free_list(opened_files, (void (*)(void *)) fclose);
    return EXIT_SUCCESS;
}
