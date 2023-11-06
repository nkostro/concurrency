package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AuctionPessimistic implements Auction {

    private Notifier notifier;

    public AuctionPessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private volatile Bid latestBid;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public boolean propose(Bid bid) {
        if (latestBid == null) {
            lock.writeLock().lock();
            if (latestBid == null) {
                latestBid = bid;
            }
            lock.writeLock().unlock();
        }
        if (bid.getPrice() > latestBid.getPrice()) {
            lock.writeLock().lock();
            try {
                if (bid.getPrice() > latestBid.getPrice()) {
                    latestBid = bid;
                    notifier.sendOutdatedMessage(latestBid);
                    return true;
                }
            } finally {
                lock.writeLock().unlock();
            }
        }

        return false;
    }

    public Bid getLatestBid() {
        lock.readLock().lock();
        try {
            return latestBid;
        } finally {
            lock.readLock().unlock();
        }
    }
}
