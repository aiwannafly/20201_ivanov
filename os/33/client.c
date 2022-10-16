#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>

#include "io_operations.h"

#define FAIL (-1)
#define STOP_MESSAGE "exit\n"
#define BUFFER_SIZE (1024)
#define SERVER_PATH "unix_sock.server"

int main() {
    int socket_fd = socket(AF_UNIX, SOCK_STREAM, 0);
    if (socket_fd == FAIL) {
        perror("=== Error in socket");
        return EXIT_FAILURE;
    }
    struct sockaddr_un serv_sockaddr;
    serv_sockaddr.sun_family = AF_UNIX;
    strcpy(serv_sockaddr.sun_path, SERVER_PATH);
    int return_value = connect(socket_fd, (struct sockaddr *) &serv_sockaddr, sizeof(serv_sockaddr));
    if (return_value == FAIL) {
        perror("=== Error in connect");
        goto FINISH;
    }
    char buffer[BUFFER_SIZE];
    while (true) {
        printf("--> ");
        char *res = fgets(buffer, BUFFER_SIZE, stdin);
        if (NULL == res) {
            perror("=== Error in fgets");
            break;
        }
        bool written = write_into_file(socket_fd, buffer, strlen(buffer));
        if (!written) {
            perror("=== Error in write");
            break;
        }
        if (strcmp(STOP_MESSAGE, res) == 0) {
            break;
        }
    }
    printf("Out of bounds\n");
    FINISH:
    {
        return_value = close(socket_fd);
        if (return_value == FAIL) {
            perror("error in close");
        }
        return EXIT_SUCCESS;
    }
}
