#include "Factory.h"

#include <iostream>

template<class Product, class Id, class Creator, class ... Args>
Factory<Product, Id, Creator, Args...> *Factory<Product, Id, Creator, Args...>::getInstance() {
    static Factory f;
    return &f;
}

template<class Product, class Id, class Creator, class ... Args>
Product *Factory<Product, Id, Creator, Args...>::createProduct(const Id &id, Args ... args) {
    auto iter = creators_.find(id);
    if (iter == creators_.end()) {
        std::cout << "not found " << id << std::endl;
        return nullptr;
    }
    return iter->second(args ...);
}

template<class Product, class Id, class Creator, class ... Args>
bool Factory<Product, Id, Creator, Args...>::registerCreator(const Id &id, Creator creator) {
    std::cout << "registered " << id << std::endl;
    creators_[id] = creator;
    return true;
}
