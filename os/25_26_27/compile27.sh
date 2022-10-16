#!/bin/bash
clang -Wall -pedantic -fsanitize=address lab27.c pipe_operations.c -o lab27
echo "Program lab27 was compiled successfully"

