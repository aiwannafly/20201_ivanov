#ifndef PRISONER_DILEMMA_FACTORY_H
#define PRISONER_DILEMMA_FACTORY_H

#include <map>

template<class Product, class Id, class ... Args>
class Factory {
public:
    typedef Product* (*Creator)(Args ...);
    static Factory *getInstance();

    Product *createProduct(const Id &id, Args ... args);

    bool registerCreator(const Id &id, Creator creator);

private:
    std::map<Id, Creator> creators_;
};

#endif //PRISONER_DILEMMA_FACTORY_H
