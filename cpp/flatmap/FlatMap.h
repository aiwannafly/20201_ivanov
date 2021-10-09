#ifndef FLATMAP_FLATMAP_H
#define FLATMAP_FLATMAP_H

#include <cstddef>
#include <string>

typedef std::string TKey;
typedef struct TValue TValue;

typedef struct TCell TCell;

class FlatMap {
public:
    FlatMap();

    ~FlatMap();

    FlatMap(const FlatMap &copy);

    FlatMap(FlatMap &&previous) noexcept;

    /*
     * Makes a swap of two flatmaps. Doesn't provide it
     * with a deep copy, just swaps required pointers.
     */
    void Swap(FlatMap &another);

    /*
     * Allocates memory for a new object and makes a deep copy of the data
     * from $another into the object
     */
    FlatMap &operator=(const FlatMap &another);

    FlatMap &operator=(FlatMap &&another) noexcept;

    /*
     * Deletes all the keys and values from the flatmap.
     */
    void Clear();

    /*
     * Finds the element with the &key and removes it from
     * the flatmap, then returns true. If the key doesn't belong
     * to the flatmap, then returns false.
     */
    bool Erase(const TKey &key);

    /*
     * Inserts the $value with the $key into the flatmap, then returns true.
     * If it's impossible to insert the element -- returns false. Also, if
     * the $key is already placed in the flatmap, it will return false and do
     * nothing else.
     */
    bool Insert(const TKey &key, const TValue &value);

    /*
     * Returns true if the &key is placed in the flatmap, false otherwise.
     */
    bool Contains(const TKey &key) const;

    /*
     * If the $key is placed in the flatmap it returns a pointer of a value
     * connected to the key.
     * Otherwise, it creates a new value by default constructor, inserts it
     * in the flatmap with the &key and returns a pointer to the value.
     */
    TValue &operator[](const TKey &key);

    /*
     * If the $key is placed in the flatmap it returns a pointer of a value
     * connected to the key.
     * Otherwise, it throws a std::out_of_range exception.
     */
    TValue &At(const TKey &key);

    /*
     * If the $key is placed in the flatmap it returns a const pointer of a
     * value connected to the key.
     * Otherwise, it throws a std::out_of_range exception.
     */
    const TValue &At(const TKey &key) const;

    /*
     * Returns count of elements placed in the flatmap.
     */
    size_t Size() const;

    /*
     * Returns true if the flatmap contains 0 elements, false otherwise.
     */
    bool Empty() const;

    friend bool operator==(const FlatMap &a, const FlatMap &b);

    friend bool operator!=(const FlatMap &a, const FlatMap &b);

private:
    TCell *cells_ = nullptr;
    size_t size_ = 0;
    size_t capacity_ = 0;
    bool InitTable();
    bool ExpandTable();
    int GetIdx(const TKey &key) const;
    bool CopyCells(const FlatMap &another);
};

#endif //FLATMAP_FLATMAP_H
