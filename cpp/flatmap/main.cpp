#include "FlatMapUnitTests.h"

int main(int argc, char *argv[]) {
    // хранить в селлах не указатели, а значения
    // binary_search надо сделать на итераторах
    // std::copy может работать на обычных указателях, так как
    // они и есть итератооры
    RunTests(argc, argv);
}
