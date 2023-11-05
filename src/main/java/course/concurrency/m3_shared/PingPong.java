package course.concurrency.m3_shared;

public class PingPong {
    private static final Object lock = new Object();

    public static void ping() {
        synchronized (lock) {
            while (!Thread.currentThread().isInterrupted()) {
                System.out.println("Ping");
                lock.notify();
                try {
                    lock.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    public static void pong() {
        synchronized (lock) {
            while (!Thread.currentThread().isInterrupted()) {
                System.out.println("Pong");
                lock.notify();
                try {
                    lock.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    public static void main(String[] args) {
        Thread t1 = new Thread(PingPong::ping);
        Thread t2 = new Thread(PingPong::pong);
        t1.start();
        t2.start();
    }
}
