package com.eaio.eproxy.rewriting.data_uri

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*
import groovy.transform.CompileStatic

import org.apache.http.HeaderElement
import org.apache.http.message.BasicHeaderValueParser
import org.junit.Before;
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import com.eaio.eproxy.Eproxy
import com.eaio.eproxy.entities.RewriteConfig;

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@CompileStatic // Needs to be @CompileStatic for some unknown reason
@RunWith(SpringJUnit4ClassRunner)
@SpringApplicationConfiguration(classes = Eproxy)
@WebIntegrationTest(randomPort = true)
class DataURIFilterIT {

    @Autowired
    DataURIFilter dataURIFilter
    
    @Before
    void 'configure filter'() {
        dataURIFilter.with {
            baseURI = 'http://bla.com/proxy/'.toURI()
            requestURI = 'http://bla-bla.com/info.html?serious=false'.toURI()
            rewriteConfig = RewriteConfig.fromString('rnw')
        }
    }
    
    @Test
    void 'rewriteData should rewrite SVG'() {
        String dataURIValue = '''image/svg+xml,<svg%20xmlns='%68ttp:%2f/www.w3.org/2000/svg'%20xmlns:xlink='%68ttp:%2f/www.w3.org/1999/xlink'><image%20xlink:hr%65f='%68ttp:%2f/leaking.via/svg-via-data'></image></svg>'''
        HeaderElement[] elements = BasicHeaderValueParser.parseElements(dataURIValue, null)
        boolean base64 = dataURIFilter.isBase64(elements)
        String data = dataURIFilter.extractData(elements, base64)
        assertThat(dataURIFilter.rewriteData(data, 'image/svg+xml', null), is('<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n    <image\n        xlink:href=\"http://bla.com/proxy/rnw-http/leaking.via/svg-via-data\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"/>\n</svg>\n'))
    }

}
