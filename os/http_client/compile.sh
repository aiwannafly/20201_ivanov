#!/bin/bash
clang -Wall -pedantic -fsanitize=address main.c -o http_client
echo "Program http_client compiled successfully"

