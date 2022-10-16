#include <netdb.h>
#include <poll.h>
#include <stdbool.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <stdio.h>
#include <unistd.h>

#define USAGE_GUIDE "usage: ./prog <url>"
#define REQUIRED_ARGC (2)
#define SUCCESS (0)
#define FAIL (-1)
#define MAX_LINES_COUNT (40)
#define HTTP_PORT "80"
#define POLL_INFINITY (-1)
#define MAX_LINE_LEN (79)
#define BUF_CAPACITY (4 * 1024)
#define PORTION_SIZE (32)
#define NEXT_SCREEN_COMMAND '\n'
#define QUIT_COMMAND 'q'

/* Makes a connection and returns
 * socket fd.
 * Prints errors in stderr */
static int connect_to_URL(const char *URL) {
    struct addrinfo hints;
    memset(&hints, 0, sizeof hints);
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_family = AF_INET;
    struct addrinfo *res;
    int return_value = getaddrinfo(URL, HTTP_PORT, &hints, &res);
    if (return_value != SUCCESS) {
        fprintf(stderr, "=== Error in getaddrinfo: %s\n", gai_strerror(return_value));
        return FAIL;
    }
    int conn_sd = socket(AF_INET, SOCK_STREAM, 0);
    if (conn_sd == FAIL) {
        perror("=== Error in socket()");
        freeaddrinfo(res);
        return FAIL;
    }
    if (connect(conn_sd, res->ai_addr, res->ai_addrlen) < 0) {
        perror("=== Error in connect()");
        freeaddrinfo(res);
        return FAIL;
    }
    freeaddrinfo(res);
    return conn_sd;
}

/* returns the count of left bytes in buffer */
static ssize_t print_screen(char *buf, ssize_t buf_size) {
    int lines_count = 0;
    int chars_in_line = 0;
    int pos = 0;
    while (lines_count < MAX_LINES_COUNT && pos < buf_size) {
        if (chars_in_line++ >= MAX_LINE_LEN) {
            chars_in_line = 0;
            lines_count++;
        }
        if (buf[pos++] == '\n') {
            lines_count++;
            chars_in_line = 0;
        }
    }
    for (size_t i = 0; i < pos; i++) {
        printf("%c", buf[i]);
    }
    printf("\n");
    memmove(buf, buf + pos, buf_size - pos);
    return buf_size - pos;
}

int main(int argc, char **argv) {
    if (argc < REQUIRED_ARGC) {
        fprintf(stderr, "%s\n", USAGE_GUIDE);
        return EXIT_FAILURE;
    }
    const char *url = argv[1];
    int socket_fd = connect_to_URL(url);
    const char *req = "GET /\r\n\r\n";
    size_t total_written_count = 0;
    while (true) {
        ssize_t written_count = write(socket_fd, req + total_written_count, strlen(req)
        - total_written_count);
        if (written_count == FAIL) {
            perror("=== Error in write");
            close(socket_fd);
            return EXIT_FAILURE;
        }
        total_written_count += written_count;
        if (total_written_count == strlen(req)) {
            break;
        }
    }
    struct pollfd fds[2];
    fds[0].fd = socket_fd;
    fds[0].events = POLLIN;
    fds[1].fd = STDIN_FILENO;
    fds[1].events = POLLIN;
    bool received_all = false;
    char buf[BUF_CAPACITY];
    ssize_t buf_index = 0;
    printf("\n=== Press ENTER to scroll down. Or 'q' to leave\n");
    while (true) {
        int return_value = poll(fds, 2, POLL_INFINITY);
        if (return_value == FAIL) {
            perror("=== Error in poll\n");
        }
        if (fds[0].revents & POLLIN && buf_index < BUF_CAPACITY - PORTION_SIZE) {
            ssize_t count_of_read = read(socket_fd, buf + buf_index, PORTION_SIZE);
            buf_index += count_of_read;
            if (count_of_read == 0) {
                received_all = true;
            }
        }
        if (fds[1].revents & POLLIN) {
            char input;
            read(STDIN_FILENO, &input, 1);
            if (input == NEXT_SCREEN_COMMAND) {
                buf_index = print_screen(buf, buf_index);
            }
            if (input == QUIT_COMMAND || received_all) {
                goto FINISH;
            }
            printf("\n=== Press ENTER to scroll down. Or 'q' to leave\n");
        }
    }
    FINISH:
    {
        printf("\n");
        int return_value = close(socket_fd);
        if (return_value == FAIL) {
            perror("=== Error in close");
        }
        return EXIT_SUCCESS;
    };
}
