#!/bin/bash
clang -Wall -pedantic -fsanitize=address main.c linked_list.c -o lab22
echo "Program lab22 compiled successfully"

