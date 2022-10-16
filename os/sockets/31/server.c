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
#define FAIL (-1)
#define MAX_CLIENTS_COUNT (32)
#define WAIT_TIME (3 * 60)
#define TIMEOUT_CODE (0)
#define STOP_MESSAGE "exit\n"

int main() {
    int listen_fd = socket(AF_UNIX, SOCK_STREAM, 0);
    if (listen_fd == FAIL) {
        perror("=== Error in socket");
        return EXIT_FAILURE;
    }
    int opt_value;
    int status = setsockopt(listen_fd, SOL_SOCKET, SO_REUSEADDR,   // Allow socket descriptor to be reuseable
                            (char *) &opt_value, sizeof(opt_value));
    if (status == FAIL) {
        perror("=== Error in setsockopt");
        status = close(listen_fd);
        if (status == FAIL) {
            perror("=== Error in close");
        }
        return EXIT_FAILURE;
    }
    status = ioctl(listen_fd, FIONBIO, (char *) &opt_value); // Set socket to be nonblocking
    if (status == FAIL) {
        perror("=== Error in ioctl");
        status = close(listen_fd);
        if (status == FAIL) {
            perror("=== Error in close");
        }
        return EXIT_FAILURE;
    }
    struct sockaddr_un address;
    memset(&address, 0, sizeof(address));
    address.sun_family = AF_UNIX;
    strcpy(address.sun_path, SERVER_PATH);
    status = bind(listen_fd, (struct sockaddr *) &address, sizeof(address));
    if (status < 0) {
        perror("=== Error in bind");
        status = close(listen_fd);
        if (status == FAIL) {
            perror("=== Error in close");
        }
        return EXIT_FAILURE;
    }
    status = listen(listen_fd, MAX_CLIENTS_COUNT);
    if (status == FAIL) {
        perror("=== Error in listen");
        status = close(listen_fd);
        if (status == FAIL) {
            perror("=== Error in close");
        }
        return EXIT_FAILURE;
    }
    fd_set master_set;
    FD_ZERO(&master_set);
    int max_sd = listen_fd;
    FD_SET(listen_fd, &master_set); // add listen_fd to our set
    struct timeval timeout;
    timeout.tv_sec = WAIT_TIME;
    timeout.tv_usec = 0;
    fd_set working_set;
    int desc_ready = 0;
    bool shutdown = false;
    int iter_count = 0;
    while (shutdown == false) {
        iter_count++;
        if (iter_count > 10) {
            break;
        }
        memcpy(&working_set, &master_set, sizeof(master_set));
        printf("=== Waiting on select()...\n");
        status = select(max_sd + 1, &working_set, NULL, NULL, &timeout);
        if (status == FAIL) {
            perror("=== Error in select");
            break;
        }
        if (status == TIMEOUT_CODE) {
            printf("=== Select timed out. End program.");
            break;
        }
        desc_ready = status;
        for (int current_fd = 0; current_fd <= max_sd && desc_ready > 0; ++current_fd) {
            if (FD_ISSET(current_fd, &working_set)) { // Check to see if this descriptor is ready
                desc_ready -= 1;
                if (current_fd == listen_fd) {
                    printf("=== Listening socket is readable\n");
                    int new_sd = 0;
                    while (true) {
                        new_sd = accept(listen_fd, NULL, NULL);
                        if (new_sd == FAIL) {
                            if (errno != EWOULDBLOCK) {
                                perror("error in accept");
                                shutdown = true;
                            }
                            break;
                        }
                        printf("=== New incoming connection - %d\n", new_sd);
                        FD_SET(new_sd, &master_set);
                        if (new_sd > max_sd) {
                            max_sd = new_sd;
                        }
                    }
                } else { // This is not the listening socket, therefore an existing connection must be readable
                    char *message = read_from_socket(current_fd);
                    if (NULL == message) {
                        perror("=== Error in read");
                        continue;
                    }
                    if (strcmp(STOP_MESSAGE, message) == 0 || strlen(message) == 0) {
                        status = close(current_fd);
                        if (status == FAIL) {
                            perror("=== Error in close");
                        }
                        FD_CLR(current_fd, &master_set);
                        if (current_fd == max_sd) {
                            max_sd -= 1;
                        }
                        if (strlen(message) == 0) {
                            continue;
                        }
                    }
                    printf("----> Client %d: ", current_fd - 3);
                    for (size_t i = 0; i < strlen(message); i++) {
                        printf("%c", toupper(message[i]));
                    }
                    free(message);
                }
            }
        }
    }
    for (int current_fd = 0; current_fd <= max_sd; ++current_fd) {
        if (FD_ISSET(current_fd, &master_set)) {
            status = close(current_fd);
            if (status == FAIL) {
                perror("=== Error in close");
            }
        }
    }
}
