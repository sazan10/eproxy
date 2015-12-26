package com.eaio.eproxy

import groovy.util.logging.Slf4j

import java.util.concurrent.TimeUnit

import org.apache.http.HttpRequestInterceptor
import org.apache.http.HttpResponseInterceptor
import org.apache.http.client.HttpClient
import org.apache.http.client.config.RequestConfig
import org.apache.http.config.Registry
import org.apache.http.config.RegistryBuilder
import org.apache.http.conn.socket.ConnectionSocketFactory
import org.apache.http.conn.socket.PlainConnectionSocketFactory
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.cache.CacheConfig
import org.apache.http.impl.client.cache.CachingHttpClients
import org.apache.http.impl.conn.*
import org.apache.http.ssl.SSLContexts
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter
import org.springframework.context.annotation.*
import org.springframework.web.servlet.DispatcherServlet
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry

import com.eaio.net.httpclient.*
import com.eaio.util.googleappengine.NotOnGoogleAppEngineOrDevserver
import com.eaio.util.googleappengine.OnGoogleAppEngineOrDevserver
import com.google.appengine.api.memcache.MemcacheService
import com.google.appengine.api.memcache.MemcacheServiceFactory

/**
 * Eproxy configuration.
 *
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id: EaioWeb.groovy 7254 2015-05-19 10:15:33Z johann $
 */
@ComponentScan('com.eaio')
@Configuration
@EnableAutoConfiguration
@EnableWebMvc
@Slf4j
class Eproxy extends WebMvcAutoConfigurationAdapter {

    // HttpClient

    @Value('${http.clientConnectionTimeout}')
    Long clientConnectionTimeout

    @Value('${http.maxTotalSockets}')
    Integer maxTotalSockets

    @Value('${http.maxSocketsPerRoute}')
    Integer maxSocketsPerRoute

    @Value('${http.connectionTimeout}')
    Integer connectionTimeout
    
    @Value('${http.connectionRequestTimeout}')
    Integer connectionRequestTimeout

    @Value('${http.readTimeout}')
    Integer readTimeout

    @Value('${http.maxRedirects}')
    Integer maxRedirects
    
    @Value('${http.followPOSTAndDELETE}')
    Boolean followPOSTAndDELETE

    @Value('${http.retryCount}')
    Integer retryCount

    @Value('${http.validateSSL}')
    Boolean validateSSL

    @Value('${http.userAgent}')
    String userAgent
    
    @Value('${http.validateAfterInactivity}')
    Integer validateAfterInactivity
    
    // SOCKS proxy
    
    @Value('${proxy.socks.host}')
    String proxySOCKSHost
    
    @Value('${proxy.socks.port}')
    Integer proxySOCKSPort // Character not supported
    
    // Cache
    
    @Value('${cache.maxCacheEntries}')
    Integer maxEntries
    
    @Value('${cache.maxObjectSize}')
    Integer maxObjectSize

    /**
     * Not necessary, hence stubbed out.
     * @see org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter#addViewControllers(org.springframework.web.servlet.config.annotation.ViewControllerRegistry)
     */
    @Override
    void addViewControllers(ViewControllerRegistry registry) {
    }

    @Lazy
    @Bean(destroyMethod = 'close')
    HttpClient httpClient() {
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register('http', PlainConnectionSocketFactory.socketFactory)
                .register('https', validateSSL ? new SSLConnectionSocketFactory(SSLContexts.createSystemDefault(), NoopHostnameVerifier.INSTANCE) : SSLConnectionSocketFactory.systemSocketFactory) // TODO Funktioniert das?
                .build()

        if (!OnGoogleAppEngineOrDevserver.CONDITION) {
            if (proxySOCKSHost && proxySOCKSPort) {
                Proxy socksProxy = new Proxy(Proxy.Type.SOCKS,
                        InetSocketAddress.createUnresolved(proxySOCKSHost, proxySOCKSPort ?: 1080I))

                socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register('http', new SOCKSProxyConnectionSocketFactory(socketFactoryRegistry.lookup('http'), socksProxy))
                        .register('https', new SOCKSProxyLayeredConnectionSocketFactory(socketFactoryRegistry.lookup('https'), socksProxy))
                        .build()
            }
        }
        
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry,
            ManagedHttpClientConnectionFactory.INSTANCE, DefaultSchemePortResolver.INSTANCE,
            new TimingDnsResolver(SystemDefaultDnsResolver.INSTANCE), clientConnectionTimeout ?: 60000L, TimeUnit.MILLISECONDS)
        connectionManager.with {
            maxTotal = maxTotalSockets ?: 32I
            defaultMaxPerRoute = maxSocketsPerRoute ?: 6I
            validateAfterInactivity = validateAfterInactivity ?: 10000I
        }

        // Timeouts and redirect counts

        RequestConfig requestConfig = RequestConfig.custom()
                .setCircularRedirectsAllowed(false)
                .setConnectTimeout(connectionTimeout ?: 10000I)
                .setConnectionRequestTimeout(connectionRequestTimeout ?: -1I)
                .setSocketTimeout(readTimeout ?: 10000I)
                .setMaxRedirects(maxRedirects ?: 10I)
                .build()
                
        log.info('RequestConfig: {}', requestConfig)

        // Timing

        TimingInterceptor timingInterceptor = new TimingInterceptor()
        
        // Caching
        
        CacheConfig cacheConfig = CacheConfig.custom()
            .setMaxCacheEntries(maxEntries ?: 1000I)
            .setMaxObjectSize(maxObjectSize ?: 1I << 20I)
            .setSharedCache(true)
            .build()
            
        // TODO: CachingExec - remove AsynchronousValidator
        // TODO: CachingExec - Via-Header-Erzeugung :(

        log.info('CacheConfig: {}', cacheConfig)
        
        HttpClientBuilder builder = CachingHttpClients.custom()
                .setCacheConfig(cacheConfig)
                //.disableAuthCaching()
                .disableCookieManagement()
                .setConnectionManager(connectionManager)
                // Retries
                .setRetryHandler(new DefaultHttpRequestRetryHandler(retryCount ?: 0I, true)) // Maybe worth removing InterruptedIOException from list
                // Fix insufficient handling of not encoded redirect URLs
                .setRedirectStrategy(followPOSTAndDELETE ? new ReEncodingLaxRedirectStrategy(reEncoding()) : new ReEncodingRedirectStrategy(reEncoding()))
                .setDefaultRequestConfig(requestConfig)
                .setUserAgent(userAgent)
                .addInterceptorFirst((HttpRequestInterceptor) timingInterceptor)
                .addInterceptorLast((HttpResponseInterceptor) timingInterceptor)

        if (maxRedirects == 0I) {
            builder.disableRedirectHandling()
        }
        if (retryCount == 0I) {
            builder.disableAutomaticRetries()
        }
        
        CloseableHttpClient client = builder.build()

        // No standard interceptors
        
        // RequestDefaultHeaders, RequestContent, RequestTargetHost, RequestClientConnControl, RequestUserAgent, RequestExpectContinue

        //        [ RequestExpectContinue, RequestClientConnControl, RequestAuthCache, RequestTargetAuthentication, RequestProxyAuthentication ].each {
        //            client.removeRequestInterceptorByClass(it)
        //        }

        client
    }

    //    Scheme createNonValidatingSSLScheme(int port = 443I) {
    //        new Scheme('https', port,
    //                new SSLSocketFactory([ isTrusted: { X509Certificate[] chain, String authType -> true } ] as TrustStrategy, new NullX509HostnameVerifier())
    //                )
    //    }
    
    @Bean
    ReEncoding reEncoding() {
        new ReEncoding()
    }

    /**
     * Only enabled in Spring Boot's server. Keep this in sync with web.xml
     */
    @Bean(name = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
    @Conditional(NotOnGoogleAppEngineOrDevserver)
    DispatcherServlet dispatcherServlet() {
        new DispatcherServlet(publishEvents: false)
    }

    @Bean
    @Conditional(OnGoogleAppEngineOrDevserver)
    @Lazy
    MemcacheService memcacheService() {
        MemcacheServiceFactory.memcacheService
    }

    @Bean
    @Conditional(NotOnGoogleAppEngineOrDevserver)
    @Lazy
    Timer timer() {
        new Timer('WebProxy', true)
    }

    static main(args) {
        SpringApplication.run(Eproxy, args)
    }

}
