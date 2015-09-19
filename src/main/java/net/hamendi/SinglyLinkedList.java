package net.hamendi;

/**
 *
 * Interface that defines the requirements for
 * the Big Data Cloud Service Technical Exercise task:
 * 
 * Implement multi-thread singly linked list in Java with following methods:
 * . push(Object o) add node to the end of list
 * . pop() remove last node
 * . insertAfter(Object o, Object after) insert node next to after node
 *
 * These methods are usually associated with stacks or LIFO queues, but most
 * existing implementations are either doubly linked lists or perform push and
 * pop on the head for performance reasons. The requirements do specify that the
 * implementation perform push and pop at the tail end and also do not specify a
 * return type for any of the methods, allowing me the freedom to decide. I have
 * therefore provided conventional return types for similar methods often found
 * in similar collection implementations.
 *
 * @author hamendi
 *
 */
public interface SinglyLinkedList<T> {

    T pop();
    boolean push(T elem);
    boolean insertAfter(T elem, T after);

}
