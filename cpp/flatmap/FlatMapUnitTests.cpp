#include "FlatMapUnitTests.h"

#include <gtest/gtest.h>
#include <string>

#include "FlatMap.h"

struct TValue {
    size_t Age;
    size_t Weight;
};

bool CmpValues(const void *c1, const void *c2) {
    auto *v1 = (TValue *) c1;
    auto *v2 = (TValue *) c2;
    if (v1->Age != v2->Age) {
        return false;
    }
    if (v1->Weight != v2->Weight) {
        return false;
    }
    return true;
}

TEST(FlatMap, Insert_Erase_Empty_Size) {
    FlatMap map;
    TValue s1 = {
            50, 50
    };
    TValue s2 = {
            30, 80
    };
    TValue s3 = {
            40, 70
    };
    TValue s4 = {
            77, 66
    };
    EXPECT_TRUE(map.Insert("Bob", s1));
    EXPECT_TRUE(map.Insert("Adam", s2));
    EXPECT_TRUE(map.Insert("John", s3));
    EXPECT_TRUE(map.Insert("Clion", s4));
    EXPECT_FALSE(map.Empty());
    EXPECT_EQ(map.Size(), 4);
    EXPECT_TRUE(map.Contains("Bob"));
    EXPECT_TRUE(map.Contains("Adam"));
    EXPECT_TRUE(map.Contains("John"));
    EXPECT_TRUE(map.Contains("Clion"));
    EXPECT_TRUE(map.Erase("Bob"));
    EXPECT_FALSE(map.Erase("Bob"));
    EXPECT_FALSE(map.Contains("Bob"));
    EXPECT_TRUE(map.Contains("Adam"));
    EXPECT_TRUE(map.Contains("John"));
    EXPECT_TRUE(map.Contains("Clion"));
    map.Clear();
    EXPECT_TRUE(map.Empty());
    EXPECT_EQ(map.Size(), 0);
}

TEST(FlatMap, Strict_Order_Inserts) {
    size_t length = 7;
    const char *bottomToUp[] = {
            "A", "B", "C", "D", "E", "F", "G"
    };
    const char *upToBottom[] = {
            "G", "F", "E", "D", "C", "B", "A"
    };
    FlatMap mapBU;
    FlatMap mapUB;
    TValue student = {10, 10};
    for (size_t i = 0; i < length; i++) {
        EXPECT_TRUE(mapBU.Insert(bottomToUp[i], student));
        EXPECT_TRUE(mapUB.Insert(upToBottom[i], student));
    }
}

TEST(FlatMap, Many_Inserts_Erases) {
    std::string basicKey = "key";
    size_t countOfKeys = 7777;
    FlatMap map;
    TValue student = {40, 40};
    for (size_t i = 0; i < countOfKeys; i++) {
        map.Insert(basicKey + std::to_string(i), student);
    }
    EXPECT_FALSE(map.Empty());
    for (size_t i = 0; i < countOfKeys; i++) {
        EXPECT_TRUE(map.Contains(basicKey + std::to_string(i)));
    }
    for (size_t i = 0; i < countOfKeys; i++) {
        map.Erase(basicKey + std::to_string(i));
    }
    for (size_t i = 0; i < countOfKeys; i++) {
        EXPECT_FALSE(map.Contains(basicKey + std::to_string(i)));
    }
    EXPECT_TRUE(map.Empty());
    for (size_t i = 0; i < countOfKeys; i++) {
        map.Insert(basicKey + std::to_string(i), student);
    }
    for (size_t i = 0; i < countOfKeys; i++) {
        EXPECT_TRUE(map.Contains(basicKey + std::to_string(i)));
    }
    EXPECT_FALSE(map.Empty());
}

TEST(FlatMap, Constructors) {
    FlatMap map1;
    TValue s1 = {
            50, 50
    };
    EXPECT_TRUE(map1.Insert("Bob", s1));
    FlatMap map2 = FlatMap(map1);
    EXPECT_TRUE(map1 == map2);
    FlatMap map3;
    map3 = map1;
    EXPECT_TRUE(map1 == map3);
    EXPECT_TRUE(map3 == map2);
}

TEST(FlatMap, Swap) {
    FlatMap map1;
    FlatMap map2;
    TValue s1 = {
            30, 70
    };
    EXPECT_TRUE(map1.Insert("Greg", s1));
    EXPECT_TRUE(map2.Insert("Jane", s1));
    FlatMap map1Copy = FlatMap(map1);
    FlatMap map2Copy = FlatMap(map2);
    map1Copy.Swap(map2Copy);
    EXPECT_TRUE(map1 == map2Copy);
    EXPECT_TRUE(map2 == map1Copy);
}

TEST(FlatMap, RVALUE_ASSIGN) {
    FlatMap map1;
    TValue s1 = {
            70, 70
    };
    EXPECT_TRUE(map1.Insert("Dave", s1));
    FlatMap map1Copy = FlatMap(map1);
    FlatMap map2 = std::move(map1);
    EXPECT_TRUE(map2 == map1Copy);
    FlatMap map3;
    EXPECT_TRUE(map3.Insert("Jimmy", s1));
    FlatMap map3Copy = FlatMap(map3);
    FlatMap map4 = FlatMap(std::move(map3));
    EXPECT_TRUE(map4 == map3Copy);
}

TEST(FlatMap, GET_BY_IDX) {
    FlatMap map1;
    TValue s1 = {
            70, 70
    };
    EXPECT_TRUE(map1.Insert("Dave", s1));
    bool cmpRes = CmpValues(&s1, &map1["Dave"]);
    EXPECT_TRUE(cmpRes);
    auto *value1 = new TValue;
    auto *value2 = &map1["Clara"];
    cmpRes = CmpValues(value1, value2);
    EXPECT_TRUE(cmpRes);
    delete value1;
    delete value2;
}

TEST(FlatMap, At) {
    FlatMap map1;
    TValue s1 = {
            70, 70
    };
    EXPECT_TRUE(map1.Insert("Dave", s1));
    bool cmpRes = CmpValues(&s1, &map1.At("Dave"));
    EXPECT_TRUE(cmpRes);
    EXPECT_THROW(map1.At("John"), std::out_of_range);
}

int RunTests(int argc, char *argv[]) {
    testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}
