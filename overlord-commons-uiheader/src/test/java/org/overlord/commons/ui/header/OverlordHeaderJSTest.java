/*
 * Copyright 2013 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.commons.ui.header;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.ServletException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link OverlordHeaderDataJS}.
 * @author eric.wittmann@redhat.com
 */
public class OverlordHeaderJSTest {

    /**
     * Test method for {@link org.overlord.commons.ui.header.OverlordHeaderDataJS#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
     * @throws URISyntaxException
     * @throws IOException
     * @throws ServletException
     */
    @Test
    public void testDoGetHttpServletRequestHttpServletResponse() throws URISyntaxException, ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        OverlordHeaderDataJS servlet = new OverlordHeaderDataJS();
        servlet.doGet(request, response);

        String headers = response.getOutputHeadersAsString();
        String content = response.getOutputAsString();

        Assert.assertEquals(EXPECTED_HEADERS, headers);
        Assert.assertEquals(EXPECTED_CONTENT, content);
    }

    private static final String EXPECTED_HEADERS =
            "Cache-control: no-cache, no-store, must-revalidate\r\n" +
            "Content-Type: text/javascript\r\n" +
            "Date: <DATE VALUE>\r\n" +
            "Expires: <DATE VALUE>\r\n" +
            "Pragma: no-cache\r\n" +
            "";
    private static final Object EXPECTED_CONTENT =
            "var OVERLORD_HEADER_DATA = {\r\n" +
            "  \"username\" : \"ewittman\"\r\n" +
            "};";

}
