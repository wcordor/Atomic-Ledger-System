package ledger;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

@Component
public class TransferRetryListener implements RetryListener {

    private final AtomicInteger totalRetries = new AtomicInteger(0);

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {

        totalRetries.incrementAndGet();
    }

    public int getAndResetRetries() {
        return totalRetries.getAndSet(0);
    }
    
}
