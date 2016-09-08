package ru.skinnyweb.client.rpc;

import com.google.gwt.user.client.Timer;

public abstract class RetryTimer {
  private static final int MAX_RETRY_DELAY = 60000 /* ms. */;

  private static final int MIN_RETRY_DELAY = 10000 /* ms. */;

  private static final int RETRY_DELAY_GROWTH_RATE = 20000 /* ms. */;

  private int retryCount;

  private Timer timer = new Timer() {
    @Override
    public void run() {
      retry();
    }
  };

  /**
   * Resets the internal counter (and the progressively lengthening retry
   * timer).
   */
  public void resetRetryCount() {
    retryCount = 0;
  }

  /**
   * Determine the amount of delay before another retry should be issued. The
   * delay after the first failure is {@link RetryTimer#MIN_RETRY_DELAY}
   * milliseconds. The delay is then increased by
   * {@link RetryTimer#RETRY_DELAY_GROWTH_RATE} milliseconds on each failure
   * until it reaches {@link RetryTimer#MAX_RETRY_DELAY} where it will remain
   * constant for any subsequent failures.
   *
   * @param count
   *          the number of failures that have occurred
   * @return the delay to use, in milliseconds
   */
  private int getRetryDelay(int count) {
    return Math.min(MAX_RETRY_DELAY, MIN_RETRY_DELAY + RETRY_DELAY_GROWTH_RATE * count);
  }

  /**
   * Clients override {@link RetryTimer#retry()} to provide the task that will
   * be retried.
   */
  protected abstract void retry();

  /**
   * Clients should call {@link RetryTimer#retryLater()} when they encounter
   * failure to schedule a retry.
   */
  protected void retryLater() {
    timer.schedule(getRetryDelay(retryCount++));
  }
}

