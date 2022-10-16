#ifndef PROXY_SERVER_SOCKET_OPERATIONS_H
#define PROXY_SERVER_SOCKET_OPERATIONS_H

int set_nonblocking(int serv_socket);

int set_reusable(int serv_socket);

int make_new_connection(char *serv_ipv4_address, int port);

#endif //PROXY_SERVER_SOCKET_OPERATIONS_H
