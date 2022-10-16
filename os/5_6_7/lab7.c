#pragma clang diagnostic push
#pragma ide diagnostic ignored "cert-err34-c"

#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <stdbool.h>
#include <sys/stat.h>
#include <sys/mman.h>
#include <unistd.h>

#define USAGE_GUIDE "USAGE: prog <text file name>"
#define DEFAULT_ROWS_COUNT (128)
#define BUFFER_SIZE (256)
#define STDIN (0)
#define WAIT_TIME (5)

#define SPLITTER "\n|=============================>"

typedef struct row_t {
    size_t end_idx;
    size_t len;
} row_t;

static row_t *get_rows(const char *file_buffer, size_t buf_size, size_t *count) {
    if (NULL == file_buffer || NULL == count) {
        return NULL;
    }
    row_t *rows = (row_t *) malloc(DEFAULT_ROWS_COUNT * sizeof(*rows));
    if (NULL == rows) {
        return NULL;
    }
    size_t array_idx = 0;
    size_t file_idx = 0;
    size_t capacity = DEFAULT_ROWS_COUNT;
    size_t len = 0;
    while (file_idx < buf_size) {
        char symbol = file_buffer[file_idx];
        if (symbol == '\n') {
            if (array_idx == capacity) {
                capacity *= 2;
                row_t *temp = realloc(rows, capacity * sizeof(*temp));
                if (NULL == temp) {
                    free(rows);
                    return NULL;
                }
            }
            rows[array_idx].end_idx = file_idx;
            rows[array_idx].len = len;
            len = 0;
            array_idx++;
        } else {
            len++;
        }
        file_idx++;
    }
    *count = array_idx;
    return rows;
}

int main(int argc, char *argv[]) {
    if (argc != 2) {
        fprintf(stderr, "%s\n", USAGE_GUIDE);
        return EXIT_FAILURE;
    }
    const char *file_name = argv[1];
    int fd = open(file_name, O_RDONLY);
    if (fd == -1) {
        perror("open failed");
        return EXIT_FAILURE;
    }
    struct stat st;
    if (fstat(fd, &st) < 0) {
        perror("fstat failed");
        close(fd);
        return EXIT_FAILURE;
    }
    size_t file_size = (size_t) st.st_size;
    char *file_buffer = (char *) mmap(NULL, file_size, // kernel chooses place for pages by itself
                                      PROT_READ, // memory protection, pages may be read
                                      MAP_SHARED, // we are not greedy
                                      fd, 0); // offset is zero
    if (file_buffer == MAP_FAILED) {
        perror("mmap failed");
        return EXIT_FAILURE;
    }
    int status = close(fd);
    if (-1 == status) {
        perror("error in close");
    }
    size_t count = 0;
    row_t *rows = get_rows(file_buffer, file_size, &count);
    if (NULL == rows) {
        perror("Memory error");
        return EXIT_FAILURE;
    }
    unsigned char buffer[BUFFER_SIZE];
    fprintf(stdout, "Enter index of a line to print or 0 to stop\n");
    fd_set file_descriptors_set_to_read;
    int max_file_descriptor = STDIN;
    FD_ZERO(&file_descriptors_set_to_read); // clear the file_descriptors_set_to_read
    FD_SET(STDIN, &file_descriptors_set_to_read); // adds STDIN to our set
    while (true) {
        struct timeval timeout;
        timeout.tv_sec = WAIT_TIME;
        timeout.tv_usec = 0;
        int status = select(max_file_descriptor + 1, &file_descriptors_set_to_read,
                            NULL, NULL, &timeout);
        if (status == 0) {
            printf("%s\n", SPLITTER);
            printf("%s\n", file_buffer);
            break;
        } else if (status == -1) {
            perror("Error in select");
            free(rows);
            munmap(file_buffer, file_size);
            return EXIT_FAILURE;
        }
        int idx = 0;
        if (FD_ISSET(STDIN, &file_descriptors_set_to_read)) {
            if (1 != fscanf(stdin, "%d", &idx)) {
                fprintf(stderr, "\nWrong input, enter a number.\n");
                free(rows);
                munmap(file_buffer, file_size);
                return EXIT_FAILURE;
            }
        } else {
            fprintf(stderr, "select error occurred\n");
            free(rows);
            munmap(file_buffer, file_size);
            return EXIT_FAILURE;
        }
        if (0 == idx) {
            break;
        } else if (idx > count) {
            fprintf(stderr, "Only %zu lines in a file\n", count);
            continue;
        } else if (idx < 0) {
            fprintf(stderr, "Idx should be positive\n");
            continue;
        }
        row_t row = rows[idx - 1];
        for (size_t i = 0; i < row.len; ++i) {
            buffer[i] = file_buffer[i + (row.end_idx - row.len)];
        }
        buffer[row.len] = '\0';
        fprintf(stdout, "%s\n", buffer);
    }
    free(rows);
    munmap(file_buffer, file_size);
    return EXIT_SUCCESS;
}
