#include "GameLife.h"

namespace {
    constexpr size_t kAliveCountForDead = 3;
    constexpr size_t kAliveCountForAlive1 = 2;
    constexpr size_t kAliveCountForAlive2 = 3;
}

size_t GameLife::getCountOfAlive(VectorField<int> &field, int x, int y) const {
    size_t count = 0;
    for (int i = x - 1; i <= x + 1; i++) {
        for (int j = y - 1; j <= y + 1; j++) {
            if (i == x && j == y) {
                continue;
            }
            if (i < 0 || i >= height_) {
                continue;
            }
            if (j < 0 || j >= width_) {
                continue;
            }
            if (field(i, j) == GameLife::ALIVE) {
                count++;
            }
        }
    }
    return count;
}

bool GameLife::proceedTick() {
    bool changed = false;
    VectorField<int> cellsCopy(*field_);
    for (size_t i = 0; i < height_; i++) {
        for (size_t j = 0; j < width_; j++) {
            size_t countOfAlive = getCountOfAlive(cellsCopy, i, j);
            if (cellsCopy(i, j) == GameLife::DEAD) {
                if (countOfAlive == kAliveCountForDead) {
                    field_->set(i, j, GameLife::ALIVE);
                }
            } else {
                if (countOfAlive == kAliveCountForAlive1 ||
                countOfAlive == kAliveCountForAlive2) {
                    continue;
                }
                field_->set(i, j, GameLife::DEAD);
            }
        }
    }
    return changed;
}
