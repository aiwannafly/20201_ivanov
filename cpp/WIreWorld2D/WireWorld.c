#include <assert.h>
#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define SIZE_LIMIT 1000000

enum conditions {
    EMPTY_CELL, ELECTRON_TAIL, ELECTRON_HEAD, CONDUCTOR
};

enum conditions getEnumCondition(char ch) {
    // turns char type condition into enum type
    switch (ch) {
        case 'A':
            return ELECTRON_HEAD;
        case 'B':
            return ELECTRON_TAIL;
        case 'C':
            return CONDUCTOR;
        default:
            return EMPTY_CELL;
    }
}

char getCharCondition(enum conditions cond) {
    // turns enum type condition into char type
    switch (cond) {
        case ELECTRON_TAIL:
            return 'B';
        case ELECTRON_HEAD:
            return 'A';
        case CONDUCTOR:
            return 'C';
        default:
            return '.';
    }
}

int findSymbol(const char array[], size_t len, char ch) {
    for (size_t i = 0; i < len; i++) {
        if (array[i] == ch) {
            return 1;
        }
    }
    return 0;
}

char* getDecodedRLE(FILE* fp, int row, int col) {
    /*
     * This function reads RLE text from a file,
     * decodes it and returns. For example:
     * 3ab2c ==> aaabcc
     *
     * Beware: it allocates memory.
     */
    char *decoded = (char*) calloc(row * col + 1, sizeof(char));
    if (decoded == NULL) {
        return NULL;
    }
    size_t decIndex = 0;

    const char allowedSymbols[] = {' ', '\n', '\t', '$', '.', 'A', 'B', 'C'};
    const size_t allSymbLen = strlen(allowedSymbols);
    size_t countOfDollars = 0; // count of rows in a rle file

    char ch = 0x00;
    size_t countOfElems = 1;
    while ((ch = (char) fgetc(fp)) != EOF && ch != '!') {
        if (ch == '\n' || ch == ' ' || ch == '\t') {
            continue;
        }
        if (!findSymbol(allowedSymbols, allSymbLen, ch) && !isdigit(ch)) {
            // Bad format of a file
            return NULL;
        } else if (ch == '$') {
            countOfDollars++;
            // skipping the line
            while (decIndex % col != 0 || decIndex == 0) {
                decoded[decIndex] = '.';
                decIndex++;
            }
            if (countOfElems > 1) {
                countOfDollars += countOfElems - 1;
                for (size_t i = 0; i < countOfElems - 1; ++i) {
                    do {
                        decoded[decIndex] = '.';
                        decIndex++;
                    } while (decIndex % col != 0);
                }
                countOfElems = 1;
            }
        } else if (isdigit(ch)) {
            ungetc(ch, fp);
            fscanf(fp, "%d", &countOfElems); // no check for fscanf due to a digit symbol existing
            if (countOfElems > row * col) {
                return NULL;
            }
        } else {
            for (size_t i = 0; i < countOfElems; ++i) {
                decoded[decIndex] = ch;
                decIndex++;
            }
            countOfElems = 1;
        }
    }
    if (countOfElems != 1) {
        return NULL;
    }
    decoded[row * col] = '\0';

    if (decIndex == 0 || ((row * col) / decIndex != 1) || row / countOfDollars != 1) {
        return NULL;
    }
    return decoded;
}

void setMatrixSize(FILE* fp, int* size) {
    // A little function which is used to get count of rows / cols from a file.
    assert(NULL != size && NULL != fp);
    char ch;
    while (!isdigit(ch = (char)fgetc(fp))) {
        if (ch != ' ' && ch != '=') {
            return;
        }
    }
    do {
        *size *= 10;
        *size += (int)ch - '0';
        if (*size > SIZE_LIMIT) {
            *size = 0;
            return;
        }
    } while (isdigit(ch = (char)fgetc(fp)));
}

enum conditions** getMatrix(FILE *fp, int* row, int* col) {
    /*
     * All this function is supposed to do
     * is to get a file with RLE text and turn it into
     * enum matrix.
     */
    assert(NULL != row && NULL != col && NULL != fp);
    char ch;
    int gotSizes = 0;

    // getting row and col
    while ((ch = (char)fgetc(fp)) != EOF) {
        if (ch == '#') {
            while ((ch = (char)fgetc(fp)) != '\n');
        }
        if (ch == 'x') {
            setMatrixSize(fp, col);
        }
        if (ch == 'y') {
            setMatrixSize(fp, row);
            gotSizes = 1;
        }
        if (gotSizes && ch == '\n') {
            break;
        }
    }

    if ((0 == *row) || (0 == *col)) return NULL;

    enum conditions** field = (enum conditions**)malloc(*row * sizeof(enum conditions*));
    if (field == NULL) {
        return NULL;
    }
    for (int i = 0; i < *row; i++) {
        field[i] = (enum conditions*)malloc(*col * sizeof(enum conditions));
        if (field[i] == NULL) {
            return NULL;
        }
    }

    char* decoded = getDecodedRLE(fp, *row, *col);
    if (NULL == decoded) {
        return NULL;
    }
    int index = 0;

    for (int i = 0; i < *row; i++) {
        for (int j = 0; j < *col; j++) {
            field[i][j] = getEnumCondition(decoded[index]);
            index++;
        }
    }
    free(decoded);
    return field;
}

int getCountOfHeads(const enum conditions** field, int row, int col, int x, int y) {
    assert(NULL != field);
    int count = 0;
    for (int i = x - 1; i <= x + 1; i++) {
        for (int j = y - 1; j <= y + 1; j++) {
            if (i == x && j == y) {
                continue;
            }
            if (i < 0 || i >= row) {
                continue;
            }

            if (j < 0 || j >= col) {
                continue;
            }

            if (field[i][j] == ELECTRON_HEAD) {
                count++;
            }
        }
    }
    return count;
}

void RLE(char* str) {
    /*
     * This function gets simple text and turns it into
     * RLE one.
     */
    assert(NULL != str);

    size_t const strLen = strlen(str);
    char *copyRLE = (char*) calloc(strLen, sizeof(char));
    int indexRLE = 0;
    int countOfRepeats = 1;
    int digits[100], numLen;

    for (int i = 0; i < strLen - 1; i++) {
        if (str[i] != str[i + 1]) {
            if (countOfRepeats > 1) {
                numLen = 0;
                while (countOfRepeats > 0) {
                    digits[numLen] = countOfRepeats % 10;
                    countOfRepeats /= 10;
                    numLen++;
                }
                for (int j = numLen - 1; j >= 0; j--) {
                    copyRLE[indexRLE] = '0' + digits[j];
                    indexRLE++;
                }

                countOfRepeats = 1;
            }
            copyRLE[indexRLE] = str[i];
            indexRLE++;
        }
        else {
            countOfRepeats++;
        }
    }
    if (str[strLen - 1] != copyRLE[indexRLE - 1]) {
        if (countOfRepeats > 1) {
            numLen = 0;

            while (countOfRepeats > 0) {
                digits[numLen] = countOfRepeats % 10;
                countOfRepeats /= 10;
                numLen++;
            }
            for (int j = numLen - 1; j >= 0; j--) {
                copyRLE[indexRLE] = '0' + digits[j];
                indexRLE++;
            }
        }
        copyRLE[indexRLE] = str[strLen - 1];
        indexRLE++;
    }

    for (int i = 0; i < indexRLE; i++) {
        str[i] = copyRLE[i];
    }
    free(copyRLE);
    str[indexRLE] = '\0';
}

void printField(FILE* fp, const enum conditions** field, int row, int col) {
    assert(NULL != field && NULL != fp);
    char* sfield = (char*)malloc((row * col * sizeof(char)) + (row * sizeof(char)));
    int index = 0;

    for (int i = 0; i < row; i++) {
        for (int j = 0; j < col; j++) {
            sfield[index] = getCharCondition(field[i][j]);
            index++;
        }
        sfield[index] = '$';
        index++;
    }

    RLE(sfield);
    const char allowedSymbols[] = {' ', '\n', '\t', '$', '.', 'A', 'B', 'C'};
    const size_t lenOfAllowedSymb = strlen(allowedSymbols);

    fprintf(fp, "x = %d, y = %d, rule = WireWorld\n", col, row);
    size_t const lenSfield = strlen(sfield);
    for (int i = 0; i < lenSfield; i++) {
        if (!findSymbol(allowedSymbols, lenOfAllowedSymb, sfield[i]) && !isdigit(sfield[i])) {
            break;
        }
        fprintf(fp, "%c", sfield[i]);
        if (i % 70 == 0 && i > 0) {
            fprintf(fp, "\n");
        }
    }
    free(sfield);
    fprintf(fp, "!");
}

void copyField(const enum conditions** field, int row, int col, enum conditions** fieldCopy) {
    assert(field != NULL && fieldCopy != NULL);

    for (int i = 0; i < row; i++) {
        for (int j = 0; j < col; j++) {
            fieldCopy[i][j] = field[i][j] + 5 - 5;
        }
    }
}

int proceedTick(const enum conditions** fieldCopy, enum conditions** field, int row, int col) {
    /*
     * Simple realization of WireWorld tick.
     * It uses rules from the source: https://en.wikipedia.org/wiki/Wireworld
     *
     * The field should't change dynamically - it is why the function needs the copy of the field too.
     */
    assert(NULL != field && NULL != fieldCopy);
    int isChanged = 0, heads;
//    mapComboBox_ = new QComboBox;
//    mapComboBox_->addItem(tr(""));
//    mapComboBox_->addItem(tr("Field 1"));
//    mapComboBox_->addItem(tr("Field 2"));
//    mapComboBox_->addItem(tr("Field 3"));
//    mapComboBox_->addItem(tr("Field 4"));
//    mapLabel_ = new QLabel(tr("&Basic fields"));
//    mapLabel_->setBuddy(mapComboBox_);
//    connect(mapComboBox_, SIGNAL(activated(int)),
//            this, SLOT(mapChanged()));
    for (int i = 0; i < row; i++) {
        for (int j = 0; j < col; j++) {
            if (fieldCopy[i][j] == ELECTRON_HEAD) {
                field[i][j] = ELECTRON_TAIL;
            }
            else if (fieldCopy[i][j] == ELECTRON_TAIL) {
                field[i][j] = CONDUCTOR;
            }
            else if (fieldCopy[i][j] == CONDUCTOR) {
                heads = getCountOfHeads(fieldCopy, row, col, i, j);
                if (heads == 1 || heads == 2) {
                    field[i][j] = ELECTRON_HEAD;
                    isChanged = 1;
                }
            }
        }
    }

    return isChanged;
}

void printMatrixToConsole(const enum conditions** field, int row, int col) {
    for (int i = 0; i < row; i++) {
        for (int j = 0; j < col; j++) {
            printf("%c", getCharCondition(field[i][j]));
        }
        printf("\n");
    }
}

int main(int argc, char* argv[]) {
    if (argc != 4) {
        printf("Please enter the name of the input file, the output file and the count of iterations to proceed.");
        return -1;
    }
    char* ptr;
    int countOfIters = strtol(argv[3], &ptr, 10);

    FILE* fp = fopen(argv[1], "r");
    if (NULL == fp) {
        fprintf(stderr, "Bad format of %s file", argv[1]);
        return -2;
    }

    int row = 0, col = 0;
    enum conditions** field = getMatrix(fp, &row, &col);
    if (NULL == field) {
        fprintf(stderr, "Bad format of %s file", argv[1]);
        return -3;
    }

    enum conditions** fieldCopy = (enum conditions**) calloc(row, sizeof(enum conditions*));
    for (int i = 0; i < row; i++) {
        fieldCopy[i] = (enum conditions*) calloc(col, sizeof(enum conditions));
    }

    fclose(fp);

    for (int i = 0; i < countOfIters; i++) {
        printMatrixToConsole((const enum conditions**) field, row, col);
        printf("\n\n");

        copyField((const enum conditions **) field, row, col, fieldCopy);
        proceedTick((const enum conditions **) fieldCopy, field, row, col);
    }

    FILE* fres = fopen(argv[2], "w");
    if (fres == NULL) {
        fprintf(stderr, "Bad format of %s file", argv[2]);
        return -4;
    }

    printField(fres, (const enum conditions **) field, row, col);
    printf("Total field was successfully written in %s file.", argv[2]);
    fclose(fres);
    for (int i = 0; i < row; i++) {
        free(field[i]);
        free(fieldCopy[i]);
    }
    free(field);
    free(fieldCopy);
    return 0;
}
