#!/bin/bash
clang -Wall -pedantic -fsanitize=address main.c linked_list.c -o strings
echo "Program strings compiled successfully"

