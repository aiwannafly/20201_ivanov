#ifndef PRISONER_DILEMMA_FACTORY_H
#define PRISONER_DILEMMA_FACTORY_H

#include <map>

template<class Product, class Id, class ... Args>
class Factory {
public:
    typedef Product* (*Creator)(Args ...);
    static Factory *getInstance() {
        static Factory f;
        return &f;
    }

    Product *createProduct(const Id &id, Args ... args) {
        auto iter = creators_.find(id);
        if (iter == creators_.end()) {
            return nullptr;
        }
        return iter->second(args ...);
    }

    bool registerCreator(const Id &id, Creator creator) {
        creators_[id] = creator;
        return true;
    }

private:
    std::map<Id, Creator> creators_;
};

#endif //PRISONER_DILEMMA_FACTORY_H
