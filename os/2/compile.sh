#!/bin/bash
clang -Wall -pedantic -fsanitize=address main.c -o california
