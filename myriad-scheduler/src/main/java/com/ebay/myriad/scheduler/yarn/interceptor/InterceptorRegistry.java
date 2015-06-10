package com.ebay.myriad.scheduler.yarn.interceptor;

/**
 * Allows registration of {@link YarnSchedulerInterceptor}.
 */
public interface InterceptorRegistry {

    public void register(YarnSchedulerInterceptor interceptor);

}
