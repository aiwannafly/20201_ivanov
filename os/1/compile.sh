#!/bin/bash
clang -Wall -pedantic -fsanitize=address main.c -o lab1
echo "Program lab1 compiled successfully"

