#!/bin/bash
clang -Wall -pedantic -fsanitize=address server.c io_operations.c -o server
echo "Program server compiled successfully"
clang -Wall -pedantic -fsanitize=address client.c io_operations.c -o client
echo "Program client compiled successfully"
