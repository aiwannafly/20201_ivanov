#!/bin/bash
clang -Wall -pedantic -fsanitize=address main.c -o lab19
echo "Program lab19 compiled successfully"

