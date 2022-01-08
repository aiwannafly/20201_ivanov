#ifndef WIREWORLD2D_GAMELIFEQT_H
#define WIREWORLD2D_GAMELIFEQT_H

#include "GameQt.h"
#include "GameLife.h"

class GameLifeQt : public GameLife, public GameQt {
public:
    GameLifeQt(size_t width, size_t height): GameLife(width, height) {};

    void set(size_t i, size_t j, int state) override {
        GameLife::set(i, j, state);
    }

    int get(size_t i, size_t j) override {
        return GameLife::get(i, j);
    }

    bool proceedTick() override {
        return GameLife::proceedTick();
    }

    bool setFieldFromFile(const std::string &fileName) override {
        return GameLife::setFieldFromFile(fileName);
    }

    QColor getCellColor(int state) override {
        if (state == GameLife::ALIVE) {
            return Qt::green;
        }
        return Qt::black;
    }

    int getCellType(QColor color) override {
        if (color == Qt::green) {
            return GameLife::ALIVE;
        }
        return GameLife::DEAD;
    }

    std::vector<QColor> getColors() override {
        return {Qt::green};
    }

};

#endif //WIREWORLD2D_GAMELIFEQT_H
