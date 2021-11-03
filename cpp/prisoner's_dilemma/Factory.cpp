#include "Factory.h"

#include <iostream>

template<class Product, class Id, class ... Args>
Factory<Product, Id, Args...> *Factory<Product, Id, Args...>::getInstance() {
    static Factory f;
    return &f;
}

template<class Product, class Id, class ... Args>
Product *Factory<Product, Id, Args...>::createProduct(const Id &id, Args ... args) {
    auto iter = creators_.find(id);
    if (iter == creators_.end()) {
        std::cout << "not found " << id << std::endl;
        return nullptr;
    }
    return iter->second(args ...);
}

template<class Product, class Id, class ... Args>
bool Factory<Product, Id, Args...>::registerCreator(const Id &id, Creator creator) {
    std::cout << "registered " << id << std::endl;
    creators_[id] = creator;
    return true;
}
