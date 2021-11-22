#!/bin/bash
clang++ -Wall -pedantic -fsanitize=address main.cpp FlatMapUnitTests.cpp -lgtest FlatMap.cpp -o flatmap

