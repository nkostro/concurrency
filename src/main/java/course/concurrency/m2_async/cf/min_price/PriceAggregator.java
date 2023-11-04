package course.concurrency.m2_async.cf.min_price;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PriceAggregator {

    private static final long SLA_MS = 2900;
    private static final Executor executor = Executors.newCachedThreadPool();

    private PriceRetriever priceRetriever = new PriceRetriever();

    public void setPriceRetriever(PriceRetriever priceRetriever) {
        this.priceRetriever = priceRetriever;
    }

    private Collection<Long> shopIds = Set.of(10l, 45l, 66l, 345l, 234l, 333l, 67l, 123l, 768l);

    public void setShops(Collection<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public double getMinPrice(long itemId) {
        CompletableFuture<Double>[] futures = shopIds.stream()
                .map(shopId -> CompletableFuture.supplyAsync(() -> priceRetriever.getPrice(itemId, shopId), executor)
                        .completeOnTimeout(Double.NaN, SLA_MS, TimeUnit.MILLISECONDS)
                        .handle((res, ex) -> res != null ? res : Double.NaN))
                .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures).thenApply(v -> Arrays.stream(futures)
                        .mapToDouble(CompletableFuture::join)
                        .filter(p -> !Double.isNaN(p))
                        .reduce(Double::min)
                        .orElse(Double.NaN)
                ).join();
    }
}
