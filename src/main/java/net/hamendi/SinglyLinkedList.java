package net.hamendi;

/**
 *
 * A multi-threaded singly linked list in Java with following methods:
 * . push(Object o) add node to the end of list
 * . pop() remove last node
 * . insertAfter(Object o, Object after) insert node next to after node
 *
 * Performing push and pop at the tail end is usually associated with stacks or 
 * LIFO queues, but most existing implementations are either doubly linked lists 
 * or perform push and pop on the head for performance reasons.
 * 
 * @author hamendi
 *
 */
public interface SinglyLinkedList<T> {

    T pop();
    boolean push(T elem);
    boolean insertAfter(T elem, T after);

}
