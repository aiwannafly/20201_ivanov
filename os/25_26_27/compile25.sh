#!/bin/bash
clang -Wall -pedantic -fsanitize=address lab25.c pipe_operations.c -o lab25
echo "Program lab25 was compiled successfully"

