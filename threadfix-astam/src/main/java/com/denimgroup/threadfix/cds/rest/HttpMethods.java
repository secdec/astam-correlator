package com.denimgroup.threadfix.cds.rest;

import com.denimgroup.threadfix.cds.rest.response.RestResponse;

import javax.annotation.Nonnull;

/**
 * Created by amohammed on 7/31/2017.
 */
public interface HttpMethods {
    <T> RestResponse<T> httpGet(@Nonnull String path);

    <T> RestResponse<T> httpGet(@Nonnull String path, @Nonnull String param);

    <T> RestResponse<T> httpPost(@Nonnull String path, byte[] entity);

    <T> RestResponse<T> httpPut(@Nonnull String path, @Nonnull String param, byte[] bytes);

    <T> RestResponse<T> httpDelete(@Nonnull String path, @Nonnull String param);
}
