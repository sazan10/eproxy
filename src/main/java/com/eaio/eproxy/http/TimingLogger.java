package com.eaio.eproxy.http;

import java.net.URI;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.*;
import org.apache.http.client.cache.HttpCacheContext;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.eaio.net.httpclient.CountingEntity;

/**
 * Logs the time and bandwidth used to complete a request.
 * <p>
 * After consumption of the response entity, {@link #log(HttpContext) pass the context to #log}.
 *
 * @author <a href="mailto:jb&#64;eaio.com">Johann Burkard</a>
 * @version $Id: TimingLogger.java 7249 2015-05-18 17:53:16Z johann $
 * @see #log(HttpContext, Logger)
 */
@Component
public class TimingLogger {
    
    private static final String STOP_WATCH = "stop_watch", COUNTING_ENTITY = "counting_entity";
    
    private Logger log = LoggerFactory.getLogger(TimingLogger.class);
    
    private long getByteCount(HttpContext context) {
        long out = -1L;
        if (context != null) {
            CountingEntity countingEntity = (CountingEntity) context.getAttribute(COUNTING_ENTITY);
            if (countingEntity != null) {
                out = countingEntity.getByteCount();
            }
        }
        return out;
    }
    
    private long getTime(HttpContext context) {
        long out = -1L;
        if (context != null) {
            StopWatch watch = (StopWatch) context.getAttribute(STOP_WATCH);
            if (watch != null) {
                out = watch.getTime();
            }
        }
        return out;
    }
    
    public void log(HttpContext context) {
        log(context, log, getTime(context), getByteCount(context));
    }
    
    public void log(HttpContext context, Logger logger) {
        log(context, logger, getTime(context), getByteCount(context));
    }
    
    private void log(HttpContext context, Logger logger, long time, long byteCount) {
        if (context != null && time >= 0 && byteCount >= 0) {
            String target = getURL(context), host = URI.create(target).getHost();
            long bytesPerSecond = Math.round(byteCount * (1000 / (double) time));
            logger.info("request to {} on {} took {} ms at {}/s transfer {} B {}{}", target, host, time, FileUtils.byteCountToDisplaySize(bytesPerSecond),
                    byteCount, FileUtils.byteCountToDisplaySize(byteCount),
                    context instanceof HttpCacheContext ? " " + ((HttpCacheContext) context).getCacheResponseStatus() : "");
        }
    }
    
    private String getURL(HttpContext context) {
        HttpUriRequest currentReq = (HttpUriRequest) context.getAttribute(HttpCoreContext.HTTP_REQUEST);
        HttpHost currentHost = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
        String currentUrl = (currentReq.getURI().isAbsolute()) ? currentReq.getURI().toString()
                : (currentHost.toURI() + currentReq.getURI());
        return currentUrl;
    }

}
