package net.hamendi;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

/**
 * Test class for the LockLessLinkedListTailLIFO
 *
 * @author hamendi
 */
public class LockLessLinkedListTailLIFOTest {

    private final static int THREADS = 8;
    private final static int TEST_SIZE = 128;
    LockLessLinkedListTailLIFO<Object> instance = new LockLessLinkedListTailLIFO<>();
    Thread[] thread = new Thread[THREADS];

    @Test
    public void testSequentialPop() {
        System.out.println("First push 5 items to list, then pop one");
        for (int i=0; i<5; i++) {
            instance.push(i);
        }
        System.out.println("Before: " + instance); //0, 1, 2, 3, 4,
        System.out.println("Poped: " + instance.pop()); //0, 1, 2, 3,
        System.out.println("After: " + instance);
        System.out.println("Inseted: " + instance.insertAfter(instance.pop(), 0)); //0, *3*, 1, 2,
        System.out.println("After: " + instance);
        System.out.println("Inseted: " + instance.insertAfter(20, 1)); //0, 3, 1, *20*, 2,
        System.out.println("After: " + instance);
        System.out.println("Inseted: " + instance.insertAfter(14, 4)); //false
        System.out.println("After: " + instance);
        System.out.println("Inseted: " + instance.insertAfter(4, 2)); //0, 3, 1, 20, 2, *4*,
        System.out.println("After: " + instance);
    }

    @Test
    public void testNull() {
        System.out.println("Null");
        new Thread(new Poper()).start();
        new Thread(new Inserter(2,0)).start(); //false
        new Thread(new Pusher(1)).start();
        new Thread(new Inserter(2,0)).start(); //false
        new Thread(new Pusher(2)).start();
        new Thread(new Inserter(101,1)).start();
        new Thread(new Poper()).start();
        new Thread(new Poper()).start();
        new Thread(new Poper()).start();
        new Thread(new Inserter(2,0)).start(); //false
    }

    @Test
    public void testSequential() {
        System.out.println("Sequential push all then pop all");
        for (int i = 0; i < TEST_SIZE; i++) {
            instance.push(i);
        }
        for (int i = TEST_SIZE-1; i > 0; i--) {
            Assert.assertTrue(instance.pop().equals(i));
        }
    }

    @Test
    public void testParallelPush() throws Exception {
        System.out.println("Parallel push");
        for (int i = 0; i < THREADS; i++) {
            thread[i] = new Pusher(i);
        }
        for (int i = 0; i < THREADS; i++) {
            thread[i].start();
        }
        for (int i = 0; i < THREADS; i++) {
            thread[i].join();
        }
    }

    @Test
    public void testParallelInsert() throws Exception {
        System.out.println("Parallel push");
        thread[0] = new Thread(new Pusher(0));
        thread[0].start();
        thread[0].join();
        for (int i = 1; i < THREADS; i++) {
            thread[i] = new Inserter(101+i,0);
        }
        for (int i = 1; i < THREADS; i++) {
            thread[i].start();
        }
        for (int i = 1; i < THREADS; i++) {
            thread[i].join();
        }
    }

    @Test
    public void testParallelPop() throws Exception {
        System.out.println("Parallel pop");
        for (int i = 0; i < THREADS; i++) {
            thread[i] = new Poper();
        }
        for (int i = 0; i < THREADS; i++) {
            thread[i].start();
        }
        for (int i = 0; i < THREADS; i++) {
            thread[i].join();
        }
    }

    @Test
    public void testParallelBoth() throws Exception {
        System.out.println("Parallel both");
        Thread[] myThreads = new Thread[2 * THREADS];
        for (int i = 0; i < THREADS; i++) {
            myThreads[i] = new Pusher(i);
            myThreads[i + THREADS] = new Poper();
        }
        for (int i = 0; i < 2 * THREADS; i++) {
            myThreads[i].start();
        }
        for (int i = 0; i < 2 * THREADS; i++) {
            myThreads[i].join();
        }
        new Thread(new Poper()).start();
        for (int i = 0; i < THREADS; i++) {
            new Thread(new Pusher(20+i)).start();
        }
        new Thread(new Poper()).start();
    }

    @Test
    public void testParallelInsertAfter() throws Exception {
        System.out.println("Parallel both");
        Thread[] myThreads = new Thread[3 * THREADS];
        for (int i = 0; i < THREADS; i++) {
            myThreads[i + 0*THREADS] = new Pusher(i);
            myThreads[i + 1*THREADS] = new Inserter(i);
            myThreads[i + 2*THREADS] = new Poper();
        }
        for (int i = 0; i < 3 * THREADS; i++) {
            myThreads[i].start();
        }
        for (int i = 0; i < 3 * THREADS; i++) {
            myThreads[i].join();
        }
        new Thread(new Poper()).start();
        for (int i = 0; i < THREADS; i++) {
            new Thread(new Pusher(20+i)).start();
        }
        new Thread(new Poper()).start();
    }

    class Pusher extends Thread {
        int value;

        Pusher(int i) {
            value = i;
        }

        public void run() {
            System.out.println("+Pushing: " + "(" + value + ") " + instance.push(value) + " QUEUE: " + instance);
        }
    }

    class Poper extends Thread {
        public void run() {
            System.out.println("-Poped: " + instance.pop() + " QUEUE: " + instance);
        }
    }

    class Inserter extends Thread {
        int value;
        int after;

        Inserter(int i, int after) {
            this.value = i;
            this.after = after;
        }

        Inserter(int i) {
            this.value = i;
            this.after = new Random().nextInt(5);
        }

        public void run() {
            System.out.println("+Inserting: " + "(" + value + ") after " + after + " - " + instance.insertAfter(value, after) + " QUEUE: " + instance);
        }
    }

}
