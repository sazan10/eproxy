package com.eaio.eproxy.cache.memcached;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.client.cache.HttpCacheEntry;
import org.apache.http.client.cache.HttpCacheStorage;
import org.apache.http.client.cache.HttpCacheUpdateCallback;
import org.apache.http.client.cache.HttpCacheUpdateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.eaio.util.googleappengine.OnGoogleAppEngineOrDevserver;
import com.google.appengine.api.capabilities.CapabilitiesService;
import com.google.appengine.api.capabilities.Capability;
import com.google.appengine.api.capabilities.CapabilityStatus;
import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.MemcacheService.IdentifiableValue;

/**
 * {@link HttpCacheStorage} implementation that uses Google App Engine's {@link AsyncMemcacheService}.
 * <p>
 * Does not throw exceptions when Memcache is down.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 * @see CapabilitiesService
 */
@Component
@Conditional(OnGoogleAppEngineOrDevserver.class)
public class AsyncMemcacheServiceHttpCacheStorage implements HttpCacheStorage {
    
    private Logger log = LoggerFactory.getLogger(AsyncMemcacheServiceHttpCacheStorage.class);

    @Autowired(required = false)
    private AsyncMemcacheService asyncMemcacheService;
    
    @Autowired(required = false)
    private CapabilitiesService capabilitiesService;
    
    @Value("${cache.memcacheMaxUpdateRetries}")
    private int maxUpdateRetries;
    
    @Value("${cache.memcacheTimeout}")
    private Integer memcacheTimeout;
    
    /**
     * @see org.apache.http.client.cache.HttpCacheStorage#putEntry(java.lang.String, org.apache.http.client.cache.HttpCacheEntry)
     */
    @Override
    public void putEntry(String key, HttpCacheEntry entry) throws IOException {
        if (capabilitiesService.getStatus(Capability.MEMCACHE).getStatus() == CapabilityStatus.ENABLED) {
            Future<Void> future = asyncMemcacheService.put(key, entry);
            awaitFutureUntilTimeout(key, future);
        }
    }

    /**
     * @see org.apache.http.client.cache.HttpCacheStorage#getEntry(java.lang.String)
     */
    @Override
    public HttpCacheEntry getEntry(String key) throws IOException {
        if (capabilitiesService.getStatus(Capability.MEMCACHE).getStatus() == CapabilityStatus.ENABLED) {
            Future<Object> future = asyncMemcacheService.get(key);
            return (HttpCacheEntry) awaitFutureUntilTimeout(key, future);
        }
        return null;
    }

    /**
     * @see org.apache.http.client.cache.HttpCacheStorage#removeEntry(java.lang.String)
     */
    @Override
    public void removeEntry(String key) throws IOException {
        if (capabilitiesService.getStatus(Capability.MEMCACHE).getStatus() == CapabilityStatus.ENABLED) {
            Future<Boolean> future = asyncMemcacheService.delete(key);
            awaitFutureUntilTimeout(key, future);
        }
    }

    /**
     * @see org.apache.http.client.cache.HttpCacheStorage#updateEntry(java.lang.String, org.apache.http.client.cache.HttpCacheUpdateCallback)
     */
    @Override
    public void updateEntry(String key, HttpCacheUpdateCallback callback)
            throws IOException, HttpCacheUpdateException {
        if (capabilitiesService.getStatus(Capability.MEMCACHE).getStatus() == CapabilityStatus.ENABLED) {
            for (int i = 0; i < maxUpdateRetries; ++i) {
                Future<IdentifiableValue> future = asyncMemcacheService.getIdentifiable(key);
                IdentifiableValue identifiable = awaitFutureUntilTimeout(key, future);
                if (identifiable == null && Thread.currentThread().isInterrupted()) {
                    return;
                }
                HttpCacheEntry oldEntry = identifiable == null ? null : (HttpCacheEntry) identifiable.getValue();
                HttpCacheEntry newEntry = callback.update(oldEntry);
                if (identifiable == null) {
                    if (newEntry != null) {
                        putEntry(key, newEntry);
                    }
                    return;
                }
                else {
                    Future<Boolean> futurePut = asyncMemcacheService.putIfUntouched(key, identifiable, newEntry);
                    Boolean stored = awaitFutureUntilTimeout(key, futurePut);
                    if (stored == null && Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    else if (Boolean.TRUE.equals(stored)) {
                        return;
                    }
                }
            }
            throw new HttpCacheUpdateException(String.format("Failed to update %s after %d tries", key, maxUpdateRetries));
        }
    }
    
    private <T> T awaitFutureUntilTimeout(String key, Future<T> future) throws IOException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            return future.get(memcacheTimeout, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException ex) {
            log.warn("operation on key {} was interrupted after {} ms", key, stopWatch.getTime());
            Thread.currentThread().interrupt();
            return null;
        }
        catch (ExecutionException ex) {
            throw new IOException(ex.getCause());
        }
        catch (TimeoutException ex) {
            stopWatch.stop();
            log.warn("operation on key {} timed out after {} ms", key, stopWatch.getTime());
            throw new IOException(ex); // ?
        }
    }

}
