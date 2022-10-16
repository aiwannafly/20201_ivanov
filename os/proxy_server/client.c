#include <arpa/inet.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <unistd.h>

#include "io_operations.h"

#define FAIL (-1)
#define STOP_MESSAGE "exit\n"
#define BUFFER_SIZE (1024)
#define PROXY_PORT (5000)

int main() {
    int socket_fd = socket(AF_INET, SOCK_STREAM, 0);
    if (socket_fd == FAIL) {
        perror("=== Error in socket");
        return EXIT_FAILURE;
    }
    struct sockaddr_in serv_sockaddr;
    serv_sockaddr.sin_family = AF_INET;
    serv_sockaddr.sin_port = htons(PROXY_PORT);
    char *ip_address = "127.0.0.1";
    if (inet_pton(AF_INET, ip_address, &serv_sockaddr.sin_addr) == FAIL) {
        perror("=== Error in inet_pton");
        return EXIT_FAILURE;
    }
    int return_value = connect(socket_fd, (struct sockaddr *) &serv_sockaddr, sizeof(serv_sockaddr));
    if (return_value == FAIL) {
        perror("=== Error in connect");
        goto FINISH;
    }
    char buffer[BUFFER_SIZE];
    while (true) {
        printf("--> ");
        char *input = fgets(buffer, BUFFER_SIZE, stdin);
        if (NULL == input) {
            perror("=== Error in fgets");
            break;
        }
        bool written = write_into_file(socket_fd, buffer, strlen(buffer));
        if (!written) {
            perror("=== Error in write");
            break;
        }
        if (strcmp(STOP_MESSAGE, input) == 0) {
            break;
        }
        char *reply_from_server = read_from_socket(socket_fd);
        if (NULL == reply_from_server) {
            perror("=== Error in read");
            continue;
        }
        if (strlen(reply_from_server) == 0) {
            printf("=== Received empty reply from server\n");
            free(reply_from_server);
            continue;
        }
        printf("Reply: %s", reply_from_server);
        free(reply_from_server);
    }
    FINISH:
    {
        return_value = close(socket_fd);
        if (return_value == FAIL) {
            perror("error in close");
        }
        return EXIT_SUCCESS;
    }
}
