#include "FlatMap.h"

#include <algorithm>
#include <cassert>
#include <stdexcept>
#include <string>

#include "util.h"
#include "util.cpp"

constexpr char keyNotExistMsg[] = "The key does not exist in the flatmap.\\n";
constexpr size_t initialCapacity = 1;
constexpr size_t expandCoefficient = 2;
constexpr int notFound = -1;

struct TCell {
    TKey *key;
    TValue *value;
};

namespace {
    int CompareKeys(const TKey *key1, const TKey *key2) {
        assert(key1);
        assert(key2);
        return (*key2).compare(*key1);
    }

    int ValuesAreEqual(const TValue *value1, const TValue *value2) {
        assert(value1);
        assert(value2);
        if ((value1->Weight == value2->Weight) &&
            (value1->Age == value2->Age)) {
            return 1;
        }
        return 0;
    }
    int CompareCells(TCell cell1, TCell cell2) {
        return CompareKeys(cell1.key, cell2.key);
    }
}

FlatMap::FlatMap() = default;

FlatMap::~FlatMap() {
    if (cells_ == nullptr) {
        return;
    }
    for (size_t i = 0; i < size_; i++) {
        delete cells_[i].key;
        delete cells_[i].value;
    }
    delete[] cells_;
    cells_ = nullptr;
    size_ = 0;
    capacity_ = 0;
}

bool FlatMap::CopyCells(const FlatMap &another) {
    if (this->size_ != another.size_) {
        return false;
    }
    for (size_t i = 0; i < size_; i++) {
        cells_[i].key = new TKey(*another.cells_[i].key);
        cells_[i].value = new TValue(*another.cells_[i].value);
    }
    return true;
}

FlatMap::FlatMap(const FlatMap &copy) {
    capacity_ = copy.capacity_;
    size_ = copy.size_;
    cells_ = new TCell[capacity_];
    assert(size_ <= capacity_);
    this->CopyCells(copy);
}

FlatMap::FlatMap(FlatMap &&previous) noexcept {
    capacity_ = previous.capacity_;
    size_ = previous.size_;
    cells_ = previous.cells_;
    previous.cells_ = nullptr;
    previous.capacity_ = 0;
    previous.size_ = 0;
}

void FlatMap::Swap(FlatMap &another) {
    if (this == &another) {
        return;
    }
    FlatMap temp = std::move(another);
    another = std::move(*this);
    *this = std::move(temp);
}

FlatMap &FlatMap::operator=(const FlatMap &another) {
    if (this == &another) {
        return *this;
    }
    this->Clear();
    capacity_ = another.capacity_;
    size_ = another.size_;
    cells_ = new TCell[capacity_];
    assert(size_ <= capacity_);
    this->CopyCells(another);
    return *this;
}

FlatMap &FlatMap::operator=(FlatMap &&another) noexcept {
    if (this == &another) {
        return *this;
    }
    capacity_ = another.capacity_;
    size_ = another.size_;
    cells_ = another.cells_;
    another.cells_ = nullptr;
    another.capacity_ = 0;
    another.size_ = 0;
    return *this;
}

void FlatMap::Clear() {
    this->~FlatMap();
}

int FlatMap::GetIdx(const TKey &key) const {
    if (size_ == 0) {
        return -1;
    }
    TCell inSearch = {};
    inSearch.key = new TKey(key);
    int result = binarySearch(cells_, 0, (int) size_ - 1, inSearch, CompareCells);
    delete inSearch.key;
    return result;
}

bool FlatMap::Insert(const TKey &key, const TValue &value) {
    int idx = this->GetIdx(key);
    if (idx != notFound) {
        // the elem with the key already exists
        return false;
    }
    if (capacity_ == 0) {
        assert(initialCapacity > 0);
        capacity_ = initialCapacity;
        cells_ = new TCell[capacity_];
    }
    if (size_ >= capacity_) {
        this->ExpandTable();
    }
    size_t insertIdx = size_;
    for (size_t i = 0; i < size_; i++) {
        assert(key != *cells_[i].key);
        if (key.compare(*cells_[i].key) > 0) {
            insertIdx = i;
            break;
        }
    }
    for (size_t i = size_; i > insertIdx; i--) {
        cells_[i] = cells_[i - 1];
    }
    cells_[insertIdx].key = new TKey(key);
    cells_[insertIdx].value = new TValue(value);
    size_++;
    return true;
}

bool FlatMap::Erase(const TKey &key) {
    if (size_ == 0) {
        return false;
    }
    int idx = this->GetIdx(key);
    if (notFound == idx) {
        return false;
    }
    delete cells_[idx].key;
    delete cells_[idx].value;
    size_--;
    for (auto i = (size_t) idx; i < size_; i++) {
        cells_[i] = cells_[i + 1];
    }
    return true;
}

bool FlatMap::Contains(const TKey &key) const {
    int idx = this->GetIdx(key);
    if (-1 == idx) {
        return false;
    }
    return true;
}

TValue &FlatMap::operator[](const TKey &key) {
    int idx = this->GetIdx(key);
    if (idx >= 0) {
        return *cells_[idx].value;
    }
    auto *value = new TValue;
    this->Insert(key, *value);
    return *value;
}

TValue &FlatMap::At(const TKey &key) {
    int idx = this->GetIdx(key);
    if (idx >= 0) {
        return *cells_[idx].value;
    }
    throw std::out_of_range(keyNotExistMsg);
}

const TValue &FlatMap::At(const TKey &key) const {
    int idx = this->GetIdx(key);
    if (idx >= 0) {
        return (const TValue &) *cells_[idx].value;
    }
    throw std::out_of_range(keyNotExistMsg);
}

void FlatMap::ExpandTable() {
    assert(capacity_);
    capacity_ *= expandCoefficient;
    auto *newCells = new TCell[capacity_];
    assert(size_ <= capacity_);
    for (size_t i = 0; i < size_; i++) {
        newCells[i] = cells_[i];
    }
    delete[] cells_;
    cells_ = newCells;
}

size_t FlatMap::Size() const {
    return size_;
}

bool FlatMap::Empty() const {
    return size_ == 0;
}

bool operator==(const FlatMap &a, const FlatMap &b) {
    if (a.capacity_ != b.capacity_) {
        return false;
    }
    if (a.size_ != b.size_) {
        return false;
    }
    for (size_t i = 0; i < a.size_; i++) {
        if (0 != CompareKeys(a.cells_[i].key, b.cells_[i].key)) {
            return false;
        }
        if (!ValuesAreEqual(a.cells_[i].value, b.cells_[i].value)) {
            return false;
        }
    }
    return true;
}

bool operator!=(const FlatMap &a, const FlatMap &b) {
    if (a == b) {
        return false;
    }
    return true;
}
