package net.hamendi;

import java.util.concurrent.atomic.*;

/**
 * To implement the methods provided in the task, i have chosen to use nonblocking synchronization to
 * eliminate locks and only rely on built-in atomic operations for synchronization because lock-free
 * data structures perform better. Non-blocking algorithms use atomic read-modify-write instructions,
 * such as compare-and-swap (CAS). Null elements not permit.
 *
 * Because the requirement asked to implement a "singly" linked list with elements added and removed
 * specifically from the "end", I did not use the easier path of implementing a stack that pushes and
 * pops from the head. Instead elements are pushed to the tail end, but the "pop" method must traverse
 * the entire list to get to the tail such that we hold both the last node as well as its predecessor
 * node, making best-case scenario at least O(n).
 *
 * JDK collections were not used, but inspiration was taken from the JDK as well as other concurrency
 * literature.
 *
 * @author hamendi
 */
public class LockLessLinkedListTailLIFO<T>  implements SinglyLinkedList<T> {

    //Sentinel nodes are never added, removed, or searched for.
    private final Node<T> head = new Node<>(null, null);
    private volatile Node<T> tail = head;

    private static final AtomicReferenceFieldUpdater<LockLessLinkedListTailLIFO, Node> tailUpdater =
            AtomicReferenceFieldUpdater.newUpdater(LockLessLinkedListTailLIFO.class, Node.class, "tail");

    private boolean casTail(Node<T> cmp, Node<T> val) {
        return tailUpdater.compareAndSet(this, cmp, val);
    }

    public LockLessLinkedListTailLIFO() {}

    /**
     * Removes the last node, at the tail of the list, and returns its value to caller.
     *
     * @return T the last element in the list, if the list is empty, then null.
     */
    public T pop() {
        outer: while (true) {
            if (head.getNext() == null) { //empty list, nothing to pop
                return null;
            }
            Node<T> oldTail = tail;
            Node<T> left = head;
            Node<T> right = left.getNext();
            //traverse the list to find the last 2 nodes, left for previous to last and right as very last
            while (right != tail) {
                if (left == null) { continue outer; }
                left = left.getNext();
                if (right == null) { continue outer; }
                right = right.getNext();
            } //end of list reached, now check stat unchanged
            if (casTail(oldTail, left)) { //checking state, true -> we're in a quiescent state and can try to proceed
                tail.casNext(right, null);
                return right.getElement();
            }
        }
    }

    /**
     * Add node to the end of list, at the tail end of the list, using the Michael-Scott
     * nonblocking algorithm.
     *
     * @param elem new node at end of list
     * @return true on success
     * @throws IllegalArgumentException if the specified element is null
     */
    public boolean push(T elem) {
        if (elem == null) {
            throw new IllegalArgumentException("Element must not be null!");
        }
        Node<T> newNode = new Node<>(elem, null);
        while (true) {
            Node<T> oldTail = tail;
            Node<T> nullEnd = oldTail.getNext();
            if (oldTail == tail) { //checking state, true -> we're in a quiescent state and can try to proceed
                if (nullEnd == null) { //checking state, true -> we're still in a quiescent state and can try to proceed with our own insertion
                    if (oldTail.casNext(nullEnd, newNode)) { //true -> linking the new node from the current last node successful
                        casTail(oldTail, newNode); //pointing tail to the new last node
                        return true; //success
                    }
                    //false -> CAS failed, state unchanged, and thread will try loop once more.
                } else { //nullEnd not null, list must be in an intermediate state, help out by moving the tail pointer forward
                    casTail(oldTail, nullEnd);
                }
            }
        }
    }

    /**
     * Adds a node right after a specific value. The list allows duplicates, so if the
     * same value is inserted more than once in the list, this method will insert the
     * new node after the first instance of the "after" element.
     *
     * Sorry for the messy code in this method, i am sure it can be made pretty, but
     * I will not optimize it for now since it is doing the job :)
     *
     * @param elem new node inserted after the "after" element.
     * @param after this node must already be in the list, else nothing is inserted.
     * @return true on success
     * @throws IllegalArgumentException if the specified element or the after element are null
     */
    public boolean insertAfter(T elem, T after) {
        if (elem == null || after == null) {
            throw new IllegalArgumentException("Element must not be null!");
        }
        if (null == head.getNext() || ! contains(after)) {
            return false; //empty list or after node not present in list
        }
        Node<T> newNode = new Node<>(elem, null);
        outer: while (true) {
            Node<T> oldTail = tail;
            Node<T> nullEnd = oldTail.getNext();
            Node<T> left = head.getNext();
            Node<T> right = left.getNext();
            if (right == null) { //list only had 1 element
                if (oldTail == tail) {
                    if (casTail(oldTail, newNode)) {
                        left.casNext(null,newNode);
                        return true;
                    }
                }
            }
            if (oldTail.getElement().equals(after)) { //last element
                if (oldTail == tail) { //checking state, true -> we're in a quiescent state and can try to proceed
                    if (nullEnd == null) { //checking state, true -> we're still in a quiescent state and can try to proceed with our own insertion
                        if (oldTail.casNext(nullEnd, newNode)) { //true -> linking the new node from the current last node successful
                            casTail(oldTail, newNode); //pointing tail to the new last node
                            return true; //success
                        }
                        //false -> CAS failed, state unchanged, and thread will try loop once more.
                    } else { //nullEnd not null, list must be in an intermediate state, help out by moving the tail pointer forward
                        casTail(oldTail, nullEnd);
                    }
                }
            }
            //traverse the list to find the last 2 nodes, left for previous to last and right as very last
            while (right != tail) {
                if (left.getElement().equals(after)) {
                    break;
                }
                if (left == null) { continue outer; }
                left = left.getNext();
                if (right == null) { continue outer; }
                right = right.getNext();
            } //end of list reached, now check stat unchanged
            if (oldTail == tail) { //checking state, true -> we're in a quiescent state and can try to proceed
                if (newNode.casNext(null, right)) {
                    left.casNext(right, newNode);
                    return true; //success
                }
            }
        }
    }

    private boolean contains(Object obj) {
        if (obj == null) {
            return false;
        }
        for (Node<T> p = head.getNext(); p != null; p = p.getNext()) {
            T element = p.getElement();
            if (element != null && obj.equals(element)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (head.getNext() != null) {
            Node<T> current = head.getNext();
            while (current != null) {
                sb.append(current).append(", ");
                if (current.getNext() == null) {
                    break;
                }
                current = current.getNext();
            }
        }
        return sb.toString();
    }

    //The internal data structure of the list
    private static class Node<T> {

        private volatile T element;
        private volatile Node<T> next;

        private static final AtomicReferenceFieldUpdater<Node, Node> nextUpdater =
                AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "next");

        public Node(T element, Node<T> next) {
            this.element = element;
            this.next = next;
        }

        public T getElement() {
            return element;
        }

        public Node<T> getNext() {
            return next;
        }

        public boolean casNext(Node<T> cmp, Node<T> val) {
            return nextUpdater.compareAndSet(this, cmp, val);
        }

        @Override
        public String toString() {
            return element.toString();
        }

    }


}