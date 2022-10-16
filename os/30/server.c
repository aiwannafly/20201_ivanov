#include <errno.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <ctype.h>

#include "io_operations.h"

#define FAIL (-1)
#define MAX_CLIENTS_COUNT (10)
#define SOCK_PATH  "unix_sock.server"

int main() {
    int serv_socket = socket(AF_UNIX, SOCK_STREAM, 0);
    if (serv_socket == FAIL) {
        perror("=== Error in socket");
        return EXIT_FAILURE;
    }
    struct sockaddr_un server_sockaddr;
    server_sockaddr.sun_family = AF_UNIX;
    strcpy(server_sockaddr.sun_path, SOCK_PATH);
    int return_value = unlink(SOCK_PATH);
    if (return_value == FAIL) {
        if (errno != ENOENT) {
            perror("=== Error in unlink");
            goto FINISH;
        }
    }
    return_value = bind(serv_socket, (struct sockaddr *) &server_sockaddr, sizeof(server_sockaddr));
    if (return_value == FAIL) {
        perror("=== Error in bind");
        goto FINISH;
    }
    return_value = listen(serv_socket, MAX_CLIENTS_COUNT);
    if (return_value == FAIL) {
        perror("=== Error in listen");
        goto FINISH;
    }
    printf("=== Server is running\n");
    while (true) {
        int connect_fd = accept(serv_socket, NULL, NULL);
        if (connect_fd == FAIL) {
            perror("=== Error in accept");
            continue;
        }
        char *message = read_from_file(connect_fd);
        if (NULL == message) {
            perror("=== Error in read");
            continue;
        }
        printf("Received message: ");
        for (size_t i = 0; i < strlen(message); i++) {
            printf("%c", toupper(message[i]));
        }
        return_value = close(connect_fd);
        if (return_value == FAIL) {
            perror("=== Error in close");
        }
        free(message);
        goto FINISH;
    }
    FINISH:
    {
        return_value = close(serv_socket);
        if (return_value == FAIL) {
            perror("=== Error in close");
        }
        return EXIT_SUCCESS;
    }
}
