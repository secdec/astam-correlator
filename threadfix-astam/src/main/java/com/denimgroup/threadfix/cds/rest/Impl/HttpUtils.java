// Copyright 2017 Secure Decisions, a division of Applied Visions, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// This material is based on research sponsored by the Department of Homeland
// Security (DHS) Science and Technology Directorate, Cyber Security Division
// (DHS S&T/CSD) via contract number HHSP233201600058C.

package com.denimgroup.threadfix.cds.rest.Impl;

import com.denimgroup.threadfix.cds.rest.cert.InstallCert;
import com.denimgroup.threadfix.cds.rest.response.ResponseParser;
import com.denimgroup.threadfix.cds.rest.response.RestResponse;
import com.denimgroup.threadfix.data.entities.AstamConfiguration;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.annotation.Nonnull;
import javax.net.ssl.SSLHandshakeException;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by amohammed on 6/20/2017.
 */
public class HttpUtils {

    public static final String JAVA_KEY_STORE_FILE = getKeyStorePath();

    private static final SanitizedLogger LOGGER = new SanitizedLogger(HttpUtils.class);

    private static int count = 0;

    private static String compId;
    private static String apiUrl;


    private static final String AUTH_HEADER_ID = "compId",
                                CONTENT_TYPE = "Content-Type",
                                ACCEPT = "Accept",
                                PROTOBUF_CONTENT = "application/x-protobuf";

    public HttpUtils(@Nonnull AstamConfiguration astamConfiguration){
        System.setProperty("javax.net.ssl.trustStore", JAVA_KEY_STORE_FILE);
        compId = astamConfiguration.getCdsCompId();
        apiUrl = astamConfiguration.getCdsApiUrl();
    }

    public <T> RestResponse<T> httpGet(@Nonnull String path){
        return httpGet(path, "");
    }

    public <T> RestResponse<T> httpGet(@Nonnull String path, @Nonnull String param){

        String urlString = makeUrl(path, param);

        if (urlString == null) {
            LOGGER.debug("The GET url could not be generated. Aborting request.");
            return ResponseParser.getErrorResponse(
                    "The GET url could not be generated and the request was not attempted.",
                    0);
        }

        CloseableHttpClient httpClient = createHttpClient();
        HttpGet httpGet = new HttpGet(urlString);
        httpGet.setHeader(AUTH_HEADER_ID, compId);
        httpGet.setHeader(ACCEPT, PROTOBUF_CONTENT);

        CloseableHttpResponse response = null;
        RestResponse restResponse = null;
        int statusCode =-1;

        try{
            response = httpClient.execute(httpGet);
            LOGGER.info(" \n Executing request: " + httpGet.toString());
            statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200){
                LOGGER.error("Request for '" + urlString
                        + "' status was " + statusCode
                        + ", not a 200 as expected. Reason: "
                        + response.getStatusLine().getReasonPhrase());
            } else {
                LOGGER.info("Request successful.");
            }

            HttpEntity entity = response.getEntity();
            if (entity != null){
                InputStream inputStream = entity.getContent();
                try {
                    restResponse = ResponseParser.getRestResponse(inputStream, statusCode, null);
                } finally {
                    EntityUtils.consume(entity);
                }
            }

        } catch (SSLHandshakeException sslHandshakeException) {
            importCert(sslHandshakeException);
        } catch (org.apache.http.conn.HttpHostConnectException hhce){
            //TODO
        }catch (IOException e){
            LOGGER.error("Executing GET request encountered IOException", e);
            restResponse = ResponseParser.getErrorResponse(
                    response.getStatusLine().getReasonPhrase(),
                    statusCode);
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                LOGGER.error("Encountered IOException when trying to close HTTP connection", e);
            }
        }

    return restResponse;
    }


    public <T> RestResponse<T> httpPost(@Nonnull String path, byte[] entity){
        String urlString = makeUrl(path);

        if(urlString == null){
            LOGGER.debug("The POST url could not be generated. Aborting request.");
            return ResponseParser.getErrorResponse(
                    "The POST url could not be generated and the request was not attempted.",
                    0);
        }

        CloseableHttpClient httpClient = createHttpClient();
        HttpPost httpPost = new HttpPost(urlString);
        httpPost.setHeader(CONTENT_TYPE, PROTOBUF_CONTENT);
        httpPost.setHeader(AUTH_HEADER_ID, compId);

        InputStreamEntity reqEntity = new InputStreamEntity(new ByteArrayInputStream(entity), -1);
        reqEntity.setContentType(PROTOBUF_CONTENT);
        httpPost.setEntity(reqEntity);

        CloseableHttpResponse response = null;
        RestResponse restResponse = null;
        int statusCode = -1;
        String location = null;

        try {
            LOGGER.info("Attempting POST request: " + urlString);
            response = httpClient.execute(httpPost);
            statusCode = response.getStatusLine().getStatusCode();

            if(statusCode != 201) {
                LOGGER.warn("POST request at '" + urlString
                        + "' status was " + statusCode
                        + ", not 201 as expected. Reason: "
                        + response.getStatusLine().getReasonPhrase());
            } else {
                location = response.getFirstHeader("Location").getValue();
                LOGGER.info("Request successful.");
            }

            restResponse = ResponseParser.getRestResponse(null, statusCode, location);

        } catch (SSLHandshakeException sslHandshakeException){
            importCert(sslHandshakeException);
        } catch (IOException e) {
            LOGGER.error("Encountered an IOException while trying to post to '"+ urlString +"' . Aborting request.");
            restResponse = ResponseParser.getErrorResponse(
                    response.getStatusLine().getReasonPhrase(),
                    statusCode);
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                LOGGER.error("The POST request encountered an IOException when attempting to close the connection");
            }
        }

        return restResponse;
    }

    public <T> RestResponse<T> httpPut(@Nonnull String path, @Nonnull String param, byte[] bytes){
        String urlString = makeUrl(path, param);

        if(urlString == null){
            LOGGER.debug("The PUT url could not be generated. Aborting request.");
            return ResponseParser.getErrorResponse(
                    "The PUT url could not be generated and the request was not attempted.",
                    0);
        }

        CloseableHttpClient httpClient = createHttpClient();
        HttpPut httpPut = new HttpPut(urlString);
        //httpPut.setHeader(CONTENT_TYPE, PROTOBUF_CONTENT);
        httpPut.setHeader(AUTH_HEADER_ID, compId);
        InputStreamEntity reqEntity = new InputStreamEntity(new ByteArrayInputStream(bytes));
        reqEntity.setContentType(PROTOBUF_CONTENT);
        httpPut.setEntity(reqEntity);
        CloseableHttpResponse response = null;
        RestResponse restResponse = null;
        int statusCode = -1;

        try {
            LOGGER.info("Attempting PUT request: " + urlString);
            response = httpClient.execute(httpPut);
            statusCode = response.getStatusLine().getStatusCode();

            if(statusCode != 204) {
                LOGGER.warn("PUT request at '"
                        + urlString + "' status was "
                        + statusCode + ", not a 204 as expected. Reason: "
                        + response.getStatusLine().getReasonPhrase());
            } else {
                LOGGER.info("Request successful.");
            }

            restResponse = ResponseParser.getRestResponse(null, statusCode, null);
        } catch (SSLHandshakeException sslHandshakeException){
            importCert(sslHandshakeException);
        } catch (IOException e) {
            LOGGER.error("Encountered an IOException while trying to update to '"
                    + urlString + "' . Aborting request.", e);
            restResponse = ResponseParser.getErrorResponse(
                    response.getStatusLine().getReasonPhrase(),
                    statusCode);
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                LOGGER.error("The PUT request encountered an IOException when attempting to close the connection", e);
            }
        }

        return restResponse;
    }

    public <T> RestResponse<T> httpDelete(@Nonnull String path, @Nonnull String param){
        String urlString = makeUrl(path, param);

        if (urlString == null) {
            LOGGER.debug("The DELETE url could not be generated. Aborting request.");
            return ResponseParser.getErrorResponse(
                    "The DELETE url could not be generated and the request was not attempted.",
                    0);
        }

        CloseableHttpClient httpClient = createHttpClient();
        HttpDelete httpDelete = new HttpDelete(urlString);
        httpDelete.setHeader(AUTH_HEADER_ID, compId);
        httpDelete.setHeader(ACCEPT, PROTOBUF_CONTENT);

        CloseableHttpResponse response = null;
        RestResponse restResponse = null;
        int statusCode =-1;

        try {
            LOGGER.info("Attempting DELETE request: " + urlString);
            response = httpClient.execute(httpDelete);
            statusCode = response.getStatusLine().getStatusCode();

            if(statusCode != 204) {
                LOGGER.warn("DELETE request at '"
                        + urlString + "' status was "
                        + statusCode + ", not a 204 as expected. Reason: "
                        + response.getStatusLine().getReasonPhrase());
            } else {
                LOGGER.info("Request successful.");
            }
            restResponse = ResponseParser.getRestResponse(null, statusCode, null);
        } catch (IOException e) {
            restResponse = ResponseParser.getErrorResponse(
                    response.getStatusLine().getReasonPhrase(),
                    statusCode);
        }

        return restResponse;
    }

    private static CloseableHttpClient createHttpClient(){
        SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
        SSLConnectionSocketFactory sslConnectionSocketFactory = null;

        try {
            //TODO: add option in UI so user can allow or deny self signed certs
            //TODO: harden this, Override isTrusted(final X509Certificate[] chain, final String authType)
            sslContextBuilder.loadTrustMaterial(new TrustSelfSignedStrategy());
            //TODO: enable hostname verification of CA generated certs. Replace with DefaultHostnameVerifier.
            sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                    sslContextBuilder.build(),
                    NoopHostnameVerifier.INSTANCE); // This is basically the same as allow all


        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("NoSuchAlgorithmException thrown while attempting to create HttpClient", e);
        } catch (KeyStoreException e) {
            LOGGER.error("KeyStoreException thrown while attempting to create HttpClient", e);
        } catch (KeyManagementException e) {
            LOGGER.error("KeyManagementException thrown while attempting to create HttpClient", e);
        }

        //TODO: Allow http option for dev purposes only. Add enable/disable option in settings.
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", sslConnectionSocketFactory )
                .build();

        HttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslConnectionSocketFactory)
                .setConnectionManager(cm)
                .build();

        return httpClient;
    }

    private String makeUrl(@Nonnull String path, @Nonnull String param){
        StringBuilder finalUrl = new StringBuilder(apiUrl);

        if (!apiUrl.endsWith("/") && path.charAt(0) != '/') {
            finalUrl.append("/");
        }

        finalUrl.append(path);

        if(param != null && !param.isEmpty()){
            String encodedParam = encode(param);
            finalUrl.append(encodedParam);
        }

        LOGGER.debug("Returning " + finalUrl.toString());
        return finalUrl.toString();
    }

    private String makeUrl(@Nonnull String path){
       return makeUrl(path, "");
    }

    private URI getURI() throws URISyntaxException {
        URI uri = new URI(apiUrl);
        return uri;
    }

    // this is necessary because Spring double-decodes %s in the URL for some reason
    public static String encodeDoublePercent(String input) {
        if (input.contains("%")) {
            input = input.replaceAll("%", "%25");
        }
        return encode(input);
    }

    public static String encode(String input) {
        try {
            return URLEncoder.encode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void importCert(SSLHandshakeException sslHandshakeException){
        if (count < 2) {
            LOGGER.warn("Unsigned certificate found. Trying to import it to Java KeyStore.");
            try {
                URI uri = getURI();
                String domain = uri.getHost();
                domain = domain.startsWith("www.") ? domain.substring(4) : domain;
                if (InstallCert.install(domain, uri.getPort())) {
                    count++;
                    LOGGER.info("Successfully imported certificate. Please try again again.");
                }
            } catch (Exception e) {
                LOGGER.error("Error when tried to import certificate. ", e);
            }
        } else {
            LOGGER.error("Unsigned certificate found. We tried to import it but was not successful." +
                    "We recommend you import server certificate to the Java cacerts keystore, or enable option in integration settings to accept all unsigned certificates. "
                    , sslHandshakeException);
        }
    }

    private static String getKeyStorePath(){
        return getKeyStoreFile().getAbsolutePath();
    }

    private static File getKeyStoreFile() {
        char SEP = File.separatorChar;
        File dir = new File(System.getProperty("java.home") + SEP
                + "lib" + SEP + "security");
        File file = new File(dir, "jssecacerts");
        if (file.isFile() == false) {
            file = new File(dir, "cacerts");
        }
        return file;
    }
}
