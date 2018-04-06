package com.denimgroup.threadfix.framework.impl.struts;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.enums.EndpointRelevanceStrictness;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import org.junit.Test;

import java.util.HashMap;

public class StrutsEndpointMatchingTests {

    private Endpoint makeEndpoint(String path) {
        return new StrutsEndpoint("", path, "GET", new HashMap<String, RouteParameter>());
    }

    private boolean isRelevant(Endpoint endpoint, String url) {
        return endpoint.isRelevant(url, EndpointRelevanceStrictness.STRICT);
    }

    @Test
    public void testStrictRelevanceMatching() {

        //  Note - other frameworks/matching tests have loose requirements regarding trailing slashes; Struts
        //  requires that trailing slashes exactly match the specified patterns (difference between namespaces
        //  and actions)

        Endpoint a = makeEndpoint("/");
        Endpoint b = makeEndpoint("/test");
        Endpoint c = makeEndpoint("/test1/test2/");
        Endpoint d = makeEndpoint("/a/b/{id}");
        Endpoint e = makeEndpoint("/a/b/{id}/c");

        assert isRelevant(a, "/");
        assert !isRelevant(a, "/test");

        assert isRelevant(b, "/test");
        assert !isRelevant(b, "/tes");
        assert !isRelevant(b, "/test/");

        assert isRelevant(c, "/test1/test2/");
        assert !isRelevant(c, "/test1/test2");
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
