package com.eaio.eproxy.api

import static com.eaio.eproxy.RequestMocks.*
import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.mock.web.DelegatingServletInputStream
import org.springframework.mock.web.DelegatingServletOutputStream
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import com.eaio.eproxy.Eproxy

/**
 * Simulates a POST request with disabled redirects.
 * 
 * @author <a href="mailto:johann@johannburkard.de">Johann Burkard</a>
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner)
@SpringApplicationConfiguration(classes = Eproxy)
@WebIntegrationTest(value = 'http.maxRedirects=0', randomPort = true)
class ProxyHTTPPOSTNoRedirectsIT {
    
    byte[] data = '''cmd=_s-xclick&encrypted=-----BEGIN+PKCS7-----%0D%0AMIAGCSqGSIb3DQEHA6CAMIACAQAxggEwMIIBLAIBADCBlDCBjjELMAkGA1UEBhMC
%0D%0AVVMxCzAJBgNVBAgTAkNBMRYwFAYDVQQHEw1Nb3VudGFpbiBWaWV3MRQwEgYDVQQK%0D%0AEwtQYXlQYWwgSW5jLjETMBEGA1UECxQKbGl2ZV9jZXJ0czERMA8GA1UEAxQIbGl2
%0D%0AZV9hcGkxHDAaBgkqhkiG9w0BCQEWDXJlQHBheXBhbC5jb20CAQAwDQYJKoZIhvcN%0D%0AAQEBBQAEgYCaQjkpIOzsGqfFE
%2BjzpGsSpU5WK4L1j2GxauzYZQxqsH9tuRaMpKPh%0D%0ASc9LTyoCH1pD1Hx5kp6KS0XUhSnI1fZXJ9LQQ%2BKiAJS0oACZ70517oaMu3uvnH2d
%0D%0A5Dooou9h9rH8Y7CaR%2BnxMSSOI0sRNGCXw5qR3kFQP8cv%2B4Qdtd8R6zCABgkqhkiG%0D%0A9w0BBwEwFAYIKoZIhvcNAwcECGbQxbFd1122oIAEggPo
%2BZ49H6QB328JfWdNnL8C%0D%0Ahi49tid86eyrH0sY8Gf5Ni2jVYsGJ9JYibR%2FDQu6tcJIUayHQVjsXSXLbpYDy6i8%0D%0AQIEHFSBBRpjIPtFVlz2tua
%2BnQ9TC%2BWMRVQKqf6XQTYJ%2FvivpqlpTaO8UjmhGw44l%0D%0Am1g2kGmhbYPymTvvccAgCMb0%2BFo4a%2BC3I81jbSzuecmPczlKo3
%2FS3uLzfCCWlqGT%0D%0Ahrsme%2Bs%2FfUtGrWQ%2FQLWF3Pvd8xoWRlEIqoXnUh4IBQhSgOdViwY5kzfrCC1MX5t9%0D%0A7Wfmc
%2BDaP6OfCbbHvx33heoANxwscF5V2KAHiV8OkZXyUE5oIyJm94HKc4GefqwC%0D%0AKnP98bbPZXb1n15k%2FtYcyzhY%2BZ%2BD
%2BqvLCHgrQuoxDXt2ff7HYI7%2FJQWJ7x1btIxn%0D%0AoTTEqUm0ROiVZnaZm%2B1oIg5tlQDWRI7mI24lnvVJfH4Ayk0h2Tk5WhTbssIiL8ZW
%0D%0A9aHnWBhMzO3Q4%2BaaWFiKnRMaCAG%2FdL%2BrGm4q9DayWkdLTF20SMHkaxIDy3jsaNsX%0D%0A29%2BHitvz7ljiV7UqkvA39lf
%2BrfHs2w%2FnMOZyOvALMLGpfnHPesIgnS6k8MRPRcKc%0D%0A9C1Ry2ZnBWm5xe8L2jY9DyakcJ12EeW76m8a2wBi%2Bo4H1rH28nXtptBetMb4j9qz
%0D%0At2Eo5RH325EWHcGTSlwUca3bDLhZLJTRUP0H5VI3pRmzl2WSvkeFp93s%2BFEQGvfB%0D%0A0rHrTyld7YkgwEcpbvMcqVj4Y8NIIoL8cAmVEi
%2FKGuVizzviRDX8jMuyLsF6Z2Dr%0D%0AIqnBUjbd%2FICWJnnOvpnPzFbRqgpfgg7M5ld9dZ0pfnLBfIFXU3q14GliRAYuoc1X
%0D%0AydgJhnQo%2FIiAG830GbuSWMCUAi0TJnERpcghIcmTDDO0TC6jAJXCbcB8BkfkPvwA%0D%0Ap3eYThb5nJP9KyUt5UL9f7RnPkeFtr
%2FIRbWhtj21WEVYiFtVn0FovsrPDZUkhhpw%0D%0AcDSeT01HiWEvuI1omGqDfBTy7ZsreAzAzvaJ8dzR3GRM7pltUkG55SxVz1a4NzNc
%0D%0AHor3%2FipTO0uX26cmb8LrZxiY%2F7WoUxfkwMRACboQQxdaNFDYjVNlCRvDEWbiezSH%0D%0AC2Ow6Q7Zs8dGDe5QtjaGYqlWcq
%2FMaEw3LfpXf6UiYtOr15FFAo9py7QC72JctL1S%0D%0A4rncJsRa9I0ANM9KIGzPz04e1K5bxRSr6SieDg3w72syMfEmF1%2FnUSSr9NwKV27R
%0D%0AeDw9%2FrxGZ1jMjI%2FUapz7uLMKd0Thc2CEWH58ukAUueCHbFTUceCeBbqo5mR%2FAjMI%0D%0AyShlmqi8qx%2FxXsk0
%2BNkdOI9UA%2BdrRaR5%2FgSCA0iBpBbo4poNA9qUamBBOrAnx6Ck%0D%0AkZ1%2FWzQ22a3NpuuP8bWTgOukdOjeNqL3dIt1SHbylbMHLIi1P
%2B1%2FxcAOOAVoY4nm%0D%0AVXG55gIntGHRpOsAZw05ULv9HyPJof1t%2Btk2qAlieza%2BUHdYsJSl%2FbatSDA3cmjN%0D%0A4vVwR
%2F4IsSavVXn9C6UVgK8cuoCu47B8v2PwkVb1yjTHwr81AUhVitm0RQQBqNri%0D%0A4YjJWZun%2BMTQ7vCbZM%2BwmbdM1EgOXx6y0sEBcQIAyhTvbaDt68m1GFf9j6lNciWp
%0D%0APhrk%2BteiBAryCvegMhacJ7PzVJA%2FaMeHqOpLpmqALVa711ZYdxfuEGYkzjPRQ7Pv%0D%0AGG3vzEjSQ4byGy4cF50W9GHBaqLcTSd6kpbbWA9Ual6o1SHB87
%2B%2B1O0%2FB8gNCHTX%0D%0AhiAy1sNzU9tTXhmskgdN1gI1%2F9zJqkf6z0Vn%2Fw%2FiTMtTwa139xzQYSXYb2qdSaab%0D%0ALL6zeT1qLPBifZqDXJREykmSwnfckW94v49
%2BtiTm8uvo7HJZ%2FsI8FI0wHZkY66m%2F%0D%0AHxZnulbPI5FGWWwS29ozc1hbQYP%2Fx6IiwQ1RyuMDOm3uTdJ6kpTvVxwCuajwuqat
%0D%0APnaLlWVa5H56gLGbi6ZdkU0JuV0a3hiDy5nTbZjqJJ0onplbu0R5qZIqatSxdHQ7%0D%0Ak6J%2BblojhzcYjIKQHzIq5NMz3h11rjPbbnDaZxm98LFBs4V7smxgECiU5qI2LLG
%2B%0D%0Ati4b4d86CmEKwaOFaSpmIYDbofiAtzpSF4shIBVtSxcfgXEcp8P%2BCQQYwg3m2mBf%0D%0A3x%2FQG2%2Fcc5bXmc2Em5BllfUpAMvpGuRVjJvQdWxkZdlCE9FRhvDvmsbrsf3mbf1Q
%0D%0AjKZcdd9SsrO4c1i3%2FpBbsJH0ipF4BwbWEUw66l4lmBSprH7kjwy7dLAd5gujlbl1%0D%0ANavs4yJwqsmuZNNNw7dzPOrlM
%2F8s1FtQJ0Vt%2B4lk9P0VIGLOFy%2F2xzDhFr9HbQRe%0D%0ArInU0CF1ut2zn6Siy4XKZKBhV8EQYgZyxUQJjK7c71oTVeBkKDML4UCx1p59gKxM
%0D%0AW9z3lwy3uCGuVXF%2BG8RTIZ2%2FkGcbc4t4bDb4tgJn8CKkKGh%2BpbhSo0JRoUucRkuJ%0D%0AlXxJMNEAAAAAAAAAAAAA
%0D%0A-----END+PKCS7-----%0D%0A'''.replaceAll('\n', '').bytes

    @Autowired
    Proxy proxy
    
    @Test
    void 'POST requests to PayPal should return a new URL'() {
        HttpServletRequest request = buildHttpServletRequest('https://www.paypal.com/cgi-bin/webscr', 'POST', { String name -> name == 'Content-Length' ? data.length as String : null },
            new DelegatingServletInputStream(new ByteArrayInputStream(data)))
        boolean statusSet = false, redirected = false
        ByteArrayOutputStream bOut = new ByteArrayOutputStream()
        HttpServletResponse response = [
            setStatus: { int status, String sc -> assertThat(status, anyOf(is(301I), is(302I))); statusSet = true },
            setHeader: { String name, String value -> if (name == 'Location') { assertThat(value, startsWith('http://fnuh.com/https/www.paypal.com/de/cgi-bin/webscr?cmd=_flow&SESSION=')); redirected = true } },
            getOutputStream: { new DelegatingServletOutputStream(bOut) },
            isCommitted: { true },
        ] as HttpServletResponse
        proxy.proxy('http', request, response)
        assertThat(statusSet, is(true))
        assertThat(redirected, is(true))
    }

}
