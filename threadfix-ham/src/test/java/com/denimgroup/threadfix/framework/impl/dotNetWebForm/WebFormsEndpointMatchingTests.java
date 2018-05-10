package com.denimgroup.threadfix.framework.impl.dotNetWebForm;

import com.denimgroup.threadfix.data.enums.EndpointRelevanceStrictness;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import org.junit.Test;

public class WebFormsEndpointMatchingTests {

    private Endpoint makeEndpoint(String path) {
        final String finalPath = path;
        return new WebFormsEndpointBase() {
            {
                this.urlPath = calculateUrlPath();
            }
            @Override
            protected String calculateUrlPath() {
                return finalPath;
            }

            @Override
            public WebFormsEndpointBase duplicate() {
                return null;
            }
        };
    }

    private boolean isRelevant(Endpoint endpoint, String url) {
        return endpoint.isRelevant(url, EndpointRelevanceStrictness.STRICT);
    }

    @Test
    public void testStrictRelevanceMatching() {

        Endpoint a = makeEndpoint("/");
        Endpoint b = makeEndpoint("/test");
        Endpoint c = makeEndpoint("/test1/test2/");
        Endpoint d = makeEndpoint("/a/b/{id}");
        Endpoint e = makeEndpoint("/a/b/{id}/c");

        assert isRelevant(a, "/");
        assert !isRelevant(a, "/test");

        assert isRelevant(b, "/test");
        assert !isRelevant(b, "/tes");
        assert isRelevant(b, "/test/");

        assert isRelevant(c, "/test1/test2");
        assert !isRelevant(c, "/test1/test2/test3");

        assert isRelevant(d, "/a/b/123");
        assert !isRelevant(d, "/a/b/123/c");
        assert !isRelevant(d, "/a/b/");

        assert isRelevant(e, "/a/b/123/c");
        assert !isRelevant(e, "/a/b/123");
        assert !isRelevant(e, "/a/b/123/d");
        assert !isRelevant(e, "/a/b/");
    }

}
