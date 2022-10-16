#!/bin/bash
clang -Wall -pedantic -fsanitize=address lab26.c pipe_operations.c -o lab26
echo "Program lab26 was compiled successfully"

