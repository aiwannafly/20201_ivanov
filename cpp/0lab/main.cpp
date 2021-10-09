#include <cassert>
#include <iostream>

typedef enum TErrorCode {
    OK, NOT_ENOUGH_SPACE
} TErrorCode;

template<class Type>

class Array {
public:
    explicit Array(size_t capacity) {
        data_ = new Type[capacity];
        assert(data_);
        capacity_ = capacity;
    };

    explicit Array(const Array<Type> *array) {
        assert(array);
        capacity_ = array->capacity_;
        size_ = array->size_;
        if (nullptr == array->data_) {
            return;
        }
        data_ = new Type[capacity_];
        assert(data_);
        for (size_t i = 0; i < size_; i++) {
            data_[i] = array->data_[i];
        }
    }

    ~Array() {
        size_ = 0;
        capacity_ = 0;
        delete[] data_;
    };

    TErrorCode pushBack(Type elem) {
        if (size_ < capacity_) {
            data_[size_++] = elem;
            return OK;
        }
        return NOT_ENOUGH_SPACE;
    };

    Type getByIdx(size_t idx) {
        assert(idx < size_);
        return data_[idx];
    };
private:
    Type *data_ = nullptr;
    size_t size_ = 0;
    size_t capacity_ = 0;
};

int main() {
    Array<int> a(5);
    for (int i = 0; i < 5; i++) {
        a.pushBack(i);
    }
    for (size_t i = 0; i < 5; i++) {
        int elem = a.getByIdx(i);
        printf("%d\n", elem);
    }
}
