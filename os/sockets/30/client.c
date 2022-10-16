#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>

#include "io_operations.h"

#define FAIL (-1)
#define MESSAGE "Hello, I am client!\n"
#define SERVER_PATH "unix_sock.server"

int main() {
    int socket_fd = socket(AF_UNIX, SOCK_STREAM, 0);
    if (socket_fd == FAIL) {
        perror("error in socket");
        return EXIT_FAILURE;
    }
    struct sockaddr_un serv_address;
    serv_address.sun_family = AF_UNIX;
    strcpy(serv_address.sun_path, SERVER_PATH);
    int status = connect(socket_fd, (struct sockaddr *) &serv_address, sizeof(serv_address));
    if (status == FAIL) {
        perror("error in connect");
        goto end;
    }
    bool written = write_into_file(socket_fd, MESSAGE, strlen(MESSAGE));
    if (!written) {
        perror("error in write");
    }
    end:
    {
        status = close(socket_fd);
        if (status == FAIL) {
            perror("error in close");
        }
        return EXIT_SUCCESS;
    }
}
