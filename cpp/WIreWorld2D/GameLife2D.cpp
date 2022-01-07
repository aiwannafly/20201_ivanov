#include "GameLife2D.h"

namespace {
    constexpr size_t kAliveCountForDead = 3;
    constexpr size_t kAliveCountForAlive1 = 2;
    constexpr size_t kAliveCountForAlive2 = 3;
}

size_t GameLife2D::getCountOfAlive(VectorField<TGameLifeCell> &field, int x, int y) const {
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
            if (field(i, j) == TGameLifeCell::ALIVE) {
                count++;
            }
        }
    }
    return count;
}

bool GameLife2D::proceedTick() {
    bool changed = false;
    VectorField<TGameLifeCell> cellsCopy(*field_);
    for (size_t i = 0; i < height_; i++) {
        for (size_t j = 0; j < width_; j++) {
            size_t countOfAlive = getCountOfAlive(cellsCopy, i, j);
            if (cellsCopy(i, j) == TGameLifeCell::DEAD) {
                if (countOfAlive == kAliveCountForDead) {
                    field_->set(i, j, TGameLifeCell::ALIVE);
                }
            } else {
                if (countOfAlive == kAliveCountForAlive1 ||
                countOfAlive == kAliveCountForAlive2) {
                    continue;
                }
                field_->set(i, j, TGameLifeCell::DEAD);
            }
        }
    }
    return changed;
}
