package com.denimgroup.threadfix.framework.impl.dotNet;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.enums.EndpointRelevanceStrictness;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class DotNetEndpointMatchingTests {

    private Action getTestAction() {
        Action testAction = new Action();
        testAction.name = "";
        testAction.attributes = new HashSet<String>();
        testAction.attributes.add("HttpGet");
        testAction.parameters = new HashMap<String, RouteParameter>();
        testAction.parametersWithTypes = new HashSet<RouteParameter>();

        return testAction;
    }

    private boolean isRelevant(Endpoint endpoint, String url) {
        return endpoint.isRelevant(url, EndpointRelevanceStrictness.STRICT);
    }

    @Test
    public void testStrictRelevanceMatching() {
        Action testAction = getTestAction();

        Endpoint a = new DotNetEndpoint("/", "", testAction);
        Endpoint b = new DotNetEndpoint("/test", "", testAction);
        Endpoint c = new DotNetEndpoint("/test1/test2/", "", testAction);
        Endpoint d = new DotNetEndpoint("/a/b/{id}", "", testAction);
        Endpoint e = new DotNetEndpoint("/a/b/{id}/c", "", testAction);

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
