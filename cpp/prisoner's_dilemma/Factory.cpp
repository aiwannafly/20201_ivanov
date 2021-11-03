#include "Factory.h"

template<class Product, class Id, class Creator, class ... Args>
Factory<Product, Id, Creator, Args...> *Factory<Product, Id, Creator, Args...>::getInstance() {
    static Factory f;
    return &f;
}

template<class Product, class Id, class Creator, class ... Args>
Product *Factory<Product, Id, Creator, Args...>::createProduct(const Id &id, Args ... args) {
    auto iter = creators_.find(id);
    if (iter == creators_.end()) {
        return nullptr;
    }
    return iter->second(args ...);
}

template<class Product, class Id, class Creator, class ... Args>
bool Factory<Product, Id, Creator, Args...>::registerCreator(const Id &id, Creator creator) {
    creators_[id] = creator;
    return true;
}
