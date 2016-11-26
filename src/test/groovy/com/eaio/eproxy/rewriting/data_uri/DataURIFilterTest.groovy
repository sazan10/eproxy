package com.eaio.eproxy.rewriting.data_uri

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*
import junitparams.JUnitParamsRunner
import junitparams.Parameters

import org.apache.http.HeaderElement
import org.apache.http.message.BasicHeaderValueParser
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith

import com.google.appengine.repackaged.org.apache.commons.codec.binary.Base64

/**
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@RunWith(JUnitParamsRunner)
class DataURIFilterTest {
    
    @Lazy
    DataURIFilter dataURIFilter = new DataURIFilter(base64: new Base64(0I, null, true))
    
    @Test
    @Parameters(method = 'extractData')
    void extractData(String dataURI, Matcher<String> valueMatcher) {
        HeaderElement[] elements = BasicHeaderValueParser.parseElements(dataURI, null)
        boolean base64 = dataURIFilter.isBase64(elements)
        assertThat(dataURIFilter.extractData(elements, base64), valueMatcher ? valueMatcher : nullValue())
    }
    
    Collection<Object[]> extractData() {
        [
            [ 'image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==', containsString('PNG') ],
            [ 'text/html;charset=utf-8,%3Chtml%3E%3Cbody%3E%3Cimg%20src%3D%22%2F%2Fbla.com%2Ffoo.png%22%3E%3C%2Fbody%3E%3C%2Fhtml%3E', is('<html><body><img src="//bla.com/foo.png"></body></html>') ],
            [ 'text/html;charset=utf-8;base64,PGh0bWw+PGJvZHk+PGltZyBzcmM9Ii8vYmxhLmNvbS9mb28ucG5nIj48L2JvZHk+PC9odG1sPg==', is('<html><body><img src="//bla.com/foo.png"></body></html>') ],
            [ 'base64,cmFo', is('rah') ],
            [ ',bullshit', is('bullshit') ],
            [ "image/svg+xml,<svg%20xmlns='%68ttp:%2f/www.w3.org/2000/svg'%20xmlns:xlink='%68ttp:%2f/www.w3.org/1999/xlink'><image%20xlink:hr%65f='%68ttp:%2f/leaking.via/svg-via-data'></image></svg>", containsString('//leaking.via') ],
            [ 'text/html;charset=utf-8;base64,PCFET0NUWVBFIGh0bWwgUFVCTElDICItLy9XM0MvL0RURCBYSFRNTCAxLjAgVHJhbnNpdGlvbmFsLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL1RSL3hodG1sMS9EVEQveGh0bWwxLXRyYW5zaXRpb25hbC5kdGQiPg0KPGh0bWwgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGh0bWwiPg0KPGhlYWQ+DQo8bWV0YSBodHRwLWVxdWl2PSJDb250ZW50LVR5cGUiIGNvbnRlbnQ9InRleHQvaHRtbDsgY2hhcnNldD1pc28tODg1OS0xIiAvPg0KPHRpdGxlPlVudGl0bGVkIERvY3VtZW50PC90aXRsZT4NCjxzdHlsZSB0eXBlPSJ0ZXh0L2NzcyI+DQo8IS0tDQpib2R5IHsNCglmb250LWZhbWlseTogQXJpYWwsIEhlbHZldGljYSwgc2Fucy1zZXJpZjsNCglmb250LXNpemU6IDEwcHQ7DQoJY29sb3I6ICMwMDAwMDA7DQoJYmFja2dyb3VuZC1jb2xvcjogI0VFRUVFRTsNCglwYWRkaW5nOiAxZW07DQp9DQpwcmUgew0KCWZvbnQtZmFtaWx5OiAiQ291cmllciBOZXciLCBDb3VyaWVyLCBtb25vOw0KCWZvbnQtc2l6ZTogMTBwdDsNCgl3aGl0ZS1zcGFjZTogcHJlOw0KfQ0KLS0+DQo8L3N0eWxlPg0KPC9oZWFkPg0KPGJvZHk+DQogPGgxPmRhdGE6IFVSSSA8L2gxPg0KIDxoMj5JRlJBTUUgRXhhbXBsZSA8L2gyPg0KIDxwPlRoaXMgSFRNTCBkb2N1bWVudCBpcyBjb250YWluZWQgd2l0aGluIGEgPGNvZGU+ZGF0YTo8L2NvZGU+IFVSSSB3aGljaCBpcyB1c2VkIGFzIHRoZSA8Y29kZT5zcmM8L2NvZGU+IGF0dHJpYnV0ZSBmb3IgdGhpcyA8Y29kZT5JRlJBTUU8L2NvZGU+OjwvcD4NCiA8cHJlPiAmbHQ7aWZyYW1lIHdpZHRoPSZxdW90OzYwMCZxdW90OyBoZWlnaHQ9JnF1b3Q7MjAwJnF1b3Q7IHNyYz0mcXVvdDtkYXRhOnRleHQvaHRtbDtjaGFyc2V0PXV0Zi04O2Jhc2U2NCxQQ0ZFVDAuLi5DOW9kRzFzUGc9PSZxdW90OyAmZ3Q7Jmx0Oy9pZnJhbWUmZ3Q7IDwvcHJlPg0KPC9ib2R5Pg0KPC9odG1sPg==', containsString('</html>') ], // From https://dopiaza.org/tools/datauri/examples/index.php
            [ 'text/html,%3Ch1%3EHello%2C%20World!%3C%2Fh1%3E', is('<h1>Hello, World!</h1>') ], // From https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/Data_URIs
            [ '''image/svg+xml,<svg%20xmlns='%68ttp:%2f/www.w3.org/2000/svg'%20xmlns:xlink='%68ttp:%2f/www.w3.org/1999/xlink'><image%20xlink:hr%65f='%68ttp:%2f/leaking.via/svg-via-data'></image></svg>''', is('''<svg xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'><image xlink:href='http://leaking.via/svg-via-data'></image></svg>''') ],
        ].collect { it as Object[] }
    }

}
