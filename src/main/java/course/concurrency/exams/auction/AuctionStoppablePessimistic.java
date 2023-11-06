package course.concurrency.exams.auction;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AuctionStoppablePessimistic implements AuctionStoppable {

    private Notifier notifier;

    private volatile Bid latestBid;

    private final ReentrantReadWriteLock proposeLock = new ReentrantReadWriteLock();
    private volatile boolean isStopped;

    public AuctionStoppablePessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    public boolean propose(Bid bid) {
        if (isStopped) {
            return false;
        }

        if (!isStopped && latestBid == null) {
            proposeLock.writeLock().lock();
            if (!isStopped && latestBid == null) {
                latestBid = bid;
            }
            proposeLock.writeLock().unlock();
            return true;
        }

        if (!isStopped && bid.getPrice() > latestBid.getPrice()) {
            proposeLock.writeLock().lock();
            try {
                if (!isStopped && bid.getPrice() > latestBid.getPrice()) {
                    latestBid = bid;
                    notifier.sendOutdatedMessage(latestBid);
                    return true;
                }
            } finally {
                proposeLock.writeLock().unlock();
            }
        }

        return false;
    }

    public Bid getLatestBid() {
        proposeLock.readLock().lock();
        try {
            return latestBid;
        } finally {
            proposeLock.readLock().unlock();
        }
    }

    public Bid stopAuction() {
        proposeLock.readLock().lock();
        try {
            isStopped = true;
            return latestBid;
        } finally {
            proposeLock.readLock().unlock();
        }
    }
}
