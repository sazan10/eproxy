package com.eaio.eproxy.cache.memcached

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import org.junit.Before
import org.junit.Test

import com.google.appengine.api.capabilities.CapabilitiesService
import com.google.appengine.api.capabilities.Capability
import com.google.appengine.api.capabilities.CapabilityState
import com.google.appengine.api.capabilities.CapabilityStatus

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
class AsyncMemcacheServiceHttpCacheStorageTest {

    @Lazy
    AsyncMemcacheServiceHttpCacheStorage asyncMemcacheServiceHttpCacheStorage
    
    @Before
    void 'set up mock service'() {
        asyncMemcacheServiceHttpCacheStorage.@capabilitiesService = [ getStatus: { Capability c -> new CapabilityState(null,
                CapabilityStatus.DISABLED, 0L) } ] as CapabilitiesService 
    }
    
    @Test
    void 'should not throw exceptions if Memcache is disabled'() {
        assertThat(asyncMemcacheServiceHttpCacheStorage.getEntry('http://foo.com/bar.html'), nullValue())
    }

}
