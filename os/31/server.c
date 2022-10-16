#include <errno.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <sys/un.h>
#include <unistd.h>
#include <ctype.h>

#include "io_operations.h"

#define SERVER_PATH "unix_sock.server"
#define SUCCESS (0)
#define FAIL (-1)
#define MAX_CLIENTS_COUNT (32)
#define WAIT_TIME (3 * 60)
#define TIMEOUT_CODE (0)
#define STOP_MESSAGE "exit\n"

/*
 * When writing a server, we need to be ready to react to many kinds of event
 * which could happen next: a new connection is made, or a client sends us a
 * request, or a client drops its connection. If we make a call to, say, accept,
 * and the call blocks, then we lose our ability to respond to other events.
 * In this case you should make a socket unblocking
 */
static int set_reusable_and_nonblocking(int serv_socket) {
    int option_value;
    int return_value = setsockopt(serv_socket, SOL_SOCKET, SO_REUSEADDR, // Allow socket descriptor to be reuseable
                                  (char *) &option_value, sizeof(option_value));
    if (return_value == FAIL) {
        perror("=== Error in setsockopt");
        return_value = close(serv_socket);
        if (return_value == FAIL) {
            perror("=== Error in close");
        }
        return FAIL;
    }
    return_value = ioctl(serv_socket, FIONBIO, (char *) &option_value); // Set socket to be nonblocking
    if (return_value == FAIL) {
        perror("=== Error in ioctl");
        return_value = close(serv_socket);
        if (return_value == FAIL) {
            perror("=== Error in close");
        }
        return FAIL;
    }
    return SUCCESS;
}

int main() {
    int serv_socket = socket(AF_UNIX, SOCK_STREAM, 0);
    if (serv_socket == FAIL) {
        perror("=== Error in socket");
        return EXIT_FAILURE;
    }
    int return_value = set_reusable_and_nonblocking(serv_socket);
    if (return_value == FAIL) {
        fprintf(stderr, "Failed to make socket reusable and nonblocking\n");
        return EXIT_FAILURE;
    }
    struct sockaddr_un address;
    address.sun_family = AF_UNIX;
    strcpy(address.sun_path, SERVER_PATH);
    return_value = unlink(SERVER_PATH);
    if (return_value == FAIL) {
        if (errno != ENOENT) {
            perror("=== Error in unlink");
        }
    }
    return_value = bind(serv_socket, (struct sockaddr *) &address, sizeof(address));
    if (return_value < 0) {
        perror("=== Error in bind");
        return_value = close(serv_socket);
        if (return_value == FAIL) {
            perror("=== Error in close");
        }
        return EXIT_FAILURE;
    }
    return_value = listen(serv_socket, MAX_CLIENTS_COUNT);
    if (return_value == FAIL) {
        perror("=== Error in listen");
        return_value = close(serv_socket);
        if (return_value == FAIL) {
            perror("=== Error in close");
        }
        return EXIT_FAILURE;
    }
    fd_set master_set;
    FD_ZERO(&master_set);
    int max_sd = serv_socket;
    FD_SET(serv_socket, &master_set); // add listen_fd to our set
    struct timeval timeout = {
            .tv_sec = WAIT_TIME,
            .tv_usec = 0
    };
    fd_set working_set;
    bool shutdown = false;
    while (shutdown == false) {
        memcpy(&working_set, &master_set, sizeof(master_set));
        printf("=== Waiting on select()...\n");
        return_value = select(max_sd + 1, &working_set, NULL, NULL, &timeout);
        if (return_value == FAIL) {
            perror("=== Error in select");
            break;
        }
        if (return_value == TIMEOUT_CODE) {
            printf("=== Select timed out. End program.");
            break;
        }
        int desc_ready = return_value;
        for (int sock_fd = 0; sock_fd <= max_sd && desc_ready > 0; ++sock_fd) {
            if (FD_ISSET(sock_fd, &working_set)) { // Check to see if this descriptor is ready
                desc_ready -= 1;
                if (sock_fd == serv_socket) {
                    printf("=== Listening socket is readable\n");
                    while (true) {
                        int new_client_sock = accept(serv_socket, NULL, NULL);
                        if (new_client_sock == FAIL) {
                            if (errno != EAGAIN) {
                                perror("=== Error in accept. Shutdown server...");
                                shutdown = true;
                            }
                            break;
                        }
                        printf("=== New incoming connection - %d\n", new_client_sock);
                        FD_SET(new_client_sock, &master_set);
                        if (new_client_sock > max_sd) {
                            max_sd = new_client_sock;
                        }
                    }
                } else { // This is not the listening socket, therefore an existing connection must be readable
                    char *message = read_from_socket(sock_fd);
                    if (NULL == message) {
                        perror("=== Error in read");
                        continue;
                    }
                    if (strcmp(STOP_MESSAGE, message) == 0 || strlen(message) == 0) {
                        return_value = close(sock_fd);
                        if (return_value == FAIL) {
                            perror("=== Error in close");
                        }
                        FD_CLR(sock_fd, &master_set);
                        if (sock_fd == max_sd) {
                            max_sd -= 1;
                        }
                        if (strlen(message) == 0) {
                            continue;
                        }
                    }
                    printf("----> Client %d: ", sock_fd - 3);
                    for (size_t i = 0; i < strlen(message); i++) {
                        printf("%c", toupper(message[i]));
                    }
                    free(message);
                }
            }
        }
    }
    for (int sock_fd = 0; sock_fd <= max_sd; ++sock_fd) {
        if (FD_ISSET(sock_fd, &master_set)) {
            return_value = close(sock_fd);
            if (return_value == FAIL) {
                perror("=== Error in close");
            }
        }
    }
}
