#pragma clang diagnostic push
#pragma ide diagnostic ignored "cert-err34-c"
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <stdbool.h>
#include <sys/stat.h>
#include <unistd.h>

#define USAGE_GUIDE "USAGE: prog <text file name>"
#define DEFAULT_ROWS_COUNT (128)
#define BUFFER_SIZE (256)

typedef struct row_t {
    size_t end_idx;
    size_t len;
} row_t;

char *read_file(int fd, size_t *buf_size) {
    if (NULL == buf_size) {
        return NULL;
    }
    struct stat file_stat;
    if (-1 == fstat(fd, &file_stat)) {
        return NULL;
    }
    off_t size = file_stat.st_size;
    char *buffer = malloc(size + 1);
    size_t bytes_read = 0;
    while (bytes_read != size) {
        long return_value = read(fd, buffer + bytes_read, size - bytes_read);
        if (0 == return_value) {
            // EOF
            break;
        }
        if (-1 == return_value) {
            if (errno == EINTR) {
                continue;
            }
        } else {
            bytes_read += return_value;
        }
    }
    buffer[size] = '\0';
    *buf_size = size;
    return buffer;
}

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
        perror("File was not opened");
        return EXIT_FAILURE;
    }
    size_t file_size = 0;
    char *file_buffer = read_file(fd, &file_size);
    if (NULL == file_buffer) {
        perror("Could not put the file into memory.\n");
        return EXIT_FAILURE;
    }
    size_t count = 0;
    row_t *rows = get_rows(file_buffer, file_size, &count);
    if (NULL == rows) {
        perror("Memory error");
        return EXIT_FAILURE;
    }
    unsigned char buffer[BUFFER_SIZE];
    fprintf(stdout, "Enter index of a line to print or 0 to stop\n");
    while (true) {
        int idx = 0;
        fprintf(stdout, "IDX -> ");
        if (1 != fscanf(stdin, "%d", &idx)) {
            fprintf(stderr, "\nWrong input, enter a number.\n");
            free(rows);
            free(file_buffer);
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
        lseek(fd, (long) row.end_idx - (long) row.len, SEEK_SET);
        size_t bytes_read = 0;
        while (bytes_read != row.len) {
            long return_value = read(fd, buffer + bytes_read, row.len - bytes_read);
            if (0 == return_value) {
                // EOF
                break;
            } else if (-1 == return_value) {
                if (errno == EINTR) {
                    continue;
                }
            } else {
                bytes_read += return_value;
            }
        }
        buffer[row.len] = '\0';
        fprintf(stdout, "%s\n", buffer);
    }
    free(file_buffer);
    free(rows);
    int status = close(fd);
    if (-1 == status) {
        perror("error in close");
    }
    return EXIT_SUCCESS;
    // 0. get the file size, make buffer[file_size]
    // 1. read file --- put the file into memory
    // 2. parse_file
    // in 6 task use select/poll functions instead of alarm
}

// Файловые системы должны реализовывать единый интерфейс, чтобы
// с ними можно было работать.
// В C это можно делать через указатель на функцию lookup
// VFS -- набор обобщенных алгоритмов (virtual file system/switch)

// Иноды одни и те же для всех
// У каждой ФС есть
// myfs_inode {
//  inode ...;
// }
/*
 * sys_open() {
 *     while() {lookup(...)}
 * }
 */

// inode {
//  owner, perm // meta
//  *read(); -- конкретный read для конкретной ФС
//  *write();
// }
/*
 * FS
 *
 * read(inode, buf)
 * --------------------
 *
 * task {
 *  uid, euid;
 *  ...
 *  inodes_array[];
 * }
 *
 * open --- открытие новой сессии работы с файлом
 *
 * file {
 *  offset
 * }
 * В иноде нету имени файла. Оно есть в dentry
 * В структуре dentry хранится inode, filename
 * У каждой иноды есть счетчик ссылок
 * Только когда счетчик занулится, файл полностью удалится с файловой системы
 * f->in = iget()
 * В иноде есть функция unlink
 * Если инода не нашлась, то это тоже закэшируется для
 * производительности.
 *
 * Чекнуть по read и write с page cache-ем.
 *
 */
//
//

