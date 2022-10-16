#!/bin/bash
clang -Wall -pedantic -fsanitize=address server.c socket_operations.c io_operations.c -o server
echo "Program server compiled successfully"
clang -Wall -pedantic -fsanitize=address client.c socket_operations.c io_operations.c -o client
echo "Program client compiled successfully"
clang -Wall -pedantic -fsanitize=address proxy_server.c socket_operations.c io_operations.c -o proxy_server
echo "Program proxy_server compiled successfully"

