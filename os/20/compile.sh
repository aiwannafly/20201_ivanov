#!/bin/bash
clang -Wall -pedantic -fsanitize=address main.c linked_list.c -o lab20
echo "Program lab20 compiled successfully"

