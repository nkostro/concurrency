package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionOptimistic implements Auction {

    private Notifier notifier;

    public AuctionOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private AtomicReference<Bid> latestBid = new AtomicReference<>();

    public boolean propose(Bid bid) {
        if (latestBid.get() == null) {
            latestBid.compareAndSet(null, bid);
        }
        Bid current = latestBid.get();
        if (bid.getPrice() > current.getPrice()) {
            do {
                if (latestBid.compareAndSet(current, bid)) {
                    notifier.sendOutdatedMessage(current);
                    return true;
                }
                current = latestBid.get();
            } while (bid.getPrice() > current.getPrice());
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid.get();
    }
}
