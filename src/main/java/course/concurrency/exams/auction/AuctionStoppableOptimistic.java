package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class AuctionStoppableOptimistic implements AuctionStoppable {

    private Notifier notifier;

    private final AtomicMarkableReference<Bid> latestBid = new AtomicMarkableReference<>(null, false);

    public AuctionStoppableOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    public boolean propose(Bid bid) {
        if (latestBid.isMarked()) {
            return false;
        }

        if (latestBid.getReference() == null) {
            if (latestBid.compareAndSet(null, bid, false, false)) {
                return true;
            }
        }

        Bid current = latestBid.getReference();
        if (!latestBid.isMarked() && bid.getPrice() > current.getPrice()) {
            do {
                if (latestBid.compareAndSet(current, bid, false, false)) {
                    notifier.sendOutdatedMessage(current);
                    return true;
                }
                current = latestBid.getReference();
            } while (!latestBid.isMarked() && bid.getPrice() > current.getPrice());
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid.getReference();
    }

    public Bid stopAuction() {
        Bid current = latestBid.getReference();
        do {
            if (latestBid.attemptMark(current, true)) {
                break;
            }
            current = latestBid.getReference();
        } while (!latestBid.isMarked());
        return current;
    }
}
