package com.denimgroup.threadfix.plugin.zap;

//import com.denimgroup.threadfix.data.interfaces.Endpoint;
//import com.denimgroup.threadfix.plugin.zap.action.LocalEndpointsAction;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.plugin.zap.action.LocalEndpointsAction;
import com.denimgroup.threadfix.remote.response.RestResponse;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
//import org.zaproxy.zap.extension.api.ApiImplementor;
//import org.zaproxy.zap.extension.api.*;
import org.zaproxy.zap.extension.api.*;
import org.zaproxy.zap.extension.threadfix.ThreadFixExtension;
import org.zaproxy.zap.extension.threadfix.ZapApiPropertiesManager;

/**
 * Created by dshannon on 2/23/15.
 */
public class ThreadFixApi extends ApiImplementor {
    private static final String IMPORT_ENDPOINTS_FROM_SOURCE = "importEndpointsFromSource";

    private static final String PARAM_THREAD_FIX_URL = "threadFixUrl";
    private static final String PARAM_API_KEY = "apiKey";
    private static final String PARAM_APP_ID = "appId";
    private static final String PARAM_TARGET_URL = "targetUrl";
    private static final String PARAM_SOURCE_FOLDER = "sourceFolder";

    ThreadFixExtension threadFixExtension;

    private static final Logger logger = Logger.getLogger(ThreadFixExtension.class);

    public ThreadFixApi(ThreadFixExtension threadFixExtension) {
        super();
        this.threadFixExtension = threadFixExtension;
        this.addApiAction(new ApiAction(IMPORT_ENDPOINTS_FROM_SOURCE, new String[]{PARAM_SOURCE_FOLDER, PARAM_TARGET_URL}));
    }

    @Override
    public String getPrefix() {
        return "threadFix";
    }


    @Override
    public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
        logger.info("Request for handleApiAction: " + name + " (params: " + params.toString() + ")");

        ZapApiPropertiesManager zapApiPropertiesManager;
        String threadFixUrl;
        String apiKey;
        String appId;
        String targetUrl;

        switch (name) {
            case IMPORT_ENDPOINTS_FROM_SOURCE:
                logger.info(IMPORT_ENDPOINTS_FROM_SOURCE);
                LocalEndpointsAction localEndpointsAction = threadFixExtension.getLocalEndpointsAction();
                Endpoint.Info[] endpoints;
                try {
                    endpoints = localEndpointsAction.getEndpoints(String.valueOf(params.get(PARAM_SOURCE_FOLDER)));
                } catch (Exception e) {
                    endpoints = null;
                }
                if ((endpoints != null) && (endpoints.length > 0)) {
                    localEndpointsAction.buildNodesFromEndpoints(endpoints);
                    localEndpointsAction.attackUrl(String.valueOf(params.get(PARAM_TARGET_URL)));
                    return ApiResponseElement.OK;
                } else {
                    throw new ApiException(ApiException.Type.INTERNAL_ERROR, "Unable to generate endpoints from source. Please check the file path.");
                }
        }
        throw new ApiException(ApiException.Type.BAD_ACTION, name);
    }

    private <T> void throwFailedResponseApiException(RestResponse<T> response, String path) throws ApiException {
        throwFailedResponseApiException(response, path, ApiException.Type.INTERNAL_ERROR);
    }

    private <T> void throwFailedResponseApiException(RestResponse<T> response, String path, ApiException.Type exceptionType) throws ApiException {
        int responseCode = response.responseCode;
        String message = response.message;

        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("Request for ThreadFix data failed at ");
        errorMessage.append(path);
        errorMessage.append(". Response Code: ");
        errorMessage.append(responseCode);
        if ((message != null) && (message.length() > 0)) {
            errorMessage.append(" Message: ");
            errorMessage.append(message);
        }

        throw new ApiException(exceptionType, errorMessage.toString());
    }

    /**
     * Gets the ThreadFix URL from the parameters or throws a Missing Parameter exception, if any
     * problems occured.
     *
     * @param params the params
     * @return the ThreadFix URL
     * @throws ApiException the api exception
     */
    private String getThreadFixUrl(JSONObject params) throws ApiException {
        try {
            return params.getString(PARAM_THREAD_FIX_URL);
        } catch (JSONException ex) {
            throw new ApiException(ApiException.Type.MISSING_PARAMETER, PARAM_THREAD_FIX_URL);
        }
    }

    /**
     * Gets the ThreadFix application id from the parameters or throws a Missing Parameter exception, if any
     * problems occured.
     *
     * @param params the params
     * @return the application id
     * @throws ApiException the api exception
     */
    private int getAppId(JSONObject params) throws ApiException {
        try {
            return params.getInt(PARAM_APP_ID);
        } catch (JSONException ex) {
            throw new ApiException(ApiException.Type.MISSING_PARAMETER, PARAM_APP_ID);
        }
    }

    /**
     * Gets the target URL from the parameters or throws a Missing Parameter exception, if any
     * problems occured.
     *
     * @param params the params
     * @return the target URL
     * @throws ApiException the api exception
     */
    private String getTargetUrl(JSONObject params) throws ApiException {
        try {
            return params.getString(PARAM_TARGET_URL);
        } catch (JSONException ex) {
            throw new ApiException(ApiException.Type.MISSING_PARAMETER, PARAM_TARGET_URL);
        }
    }

    /**
     * Gets the source folder from the parameters or throws a Missing Parameter exception, if any
     * problems occured.
     *
     * @param params the params
     * @return the source folder
     * @throws ApiException the api exception
     */
    private String getSourceFolder(JSONObject params) throws ApiException {
        try {
            return params.getString(PARAM_SOURCE_FOLDER);
        } catch (JSONException ex) {
            throw new ApiException(ApiException.Type.MISSING_PARAMETER, PARAM_SOURCE_FOLDER);
        }
    }
}
