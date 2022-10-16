#!/bin/bash
clang -Wall -pedantic -fsanitize=address main.c -o lab16
echo "Program lab16 compiled successfully"

