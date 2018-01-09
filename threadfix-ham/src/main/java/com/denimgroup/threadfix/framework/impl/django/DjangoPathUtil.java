package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.framework.util.PathUtil;

public class DjangoPathUtil {

    public static String combine(String begin, String end) {
        return combine(begin, end, true);
    }

    public static String combine(String begin, String end, boolean prefixWithSlash) {

        String result = PathUtil.combine(begin, end, prefixWithSlash);
        //  When '/^' and '/^route' are combined they make '/^/^route', which is incorrect
        result = result.replaceAll("\\/\\^\\/\\^", "\\/\\^");

        return result;
    }

}
