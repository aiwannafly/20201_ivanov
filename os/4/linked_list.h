#ifndef LINKED_LIST_H
#define LINKED_LIST_H

#include <stdbool.h>
#include <stddef.h>
#include <stdio.h>

typedef struct list_node_t {
    struct list_node_t *next;
    struct list_node_t *prev;
    void *value;
} list_node_t;

typedef struct list_t {
    list_node_t *head;
    list_node_t *tail;
    size_t len;
} list_t;

list_node_t *init_list_node(const void *value);

list_t *init_list();

/*
 * Sets the list_node_t in a position of a list_t->tail.
 */
void push_to_tail(list_t *list, list_node_t *node);

/*
 * Adds new list_node_t to the tail of
 * the list_t.
 */
bool append_list_node(list_t *list, list_node_t *new_node);

bool append(list_t *list, const void *value);

/*
 * Cuts off the head of the list_t and returns it.
 * So the size of a list_t decrements.
 */
list_node_t *pop_list_node(list_t *list);

void *pop(list_t *list);

/*
 * Insertion position is a node from the main list, which should be freed
 * and replaced with nodes of the sublist.
 */
list_t *insert_sub_list(list_t *main_list, list_t *sub_list, list_node_t* insert_pos);

void print_list(FILE *fp_output, const list_t *list, void (*print_list_node_value)(FILE *, void *));

void free_list(list_t *list, void (*free_value)(void *));

#endif //LINKED_LIST_H
