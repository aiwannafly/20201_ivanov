#!/bin/bash
clang -Wall -pedantic -fsanitize=address main.c -o open_file
echo "Program open_file was compiled"
