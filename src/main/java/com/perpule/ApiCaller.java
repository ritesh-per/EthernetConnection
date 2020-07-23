package com.perpule;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ApiCaller {

    public enum RequestMethod {
        GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE;
    }

    private RequestMethod requestMethod;
    private String url;
    private Integer connectionTimeout = 30000;
    private Integer readTimeout = 30000;
    private Map<String,String> headers = new HashMap<String, String>();
    private String data;


    private ApiCaller(){}

    public static ApiCaller getInstance(){
        return new ApiCaller();
    }

    public ApiCaller setRequestMethod(RequestMethod requestMethod){
        this.requestMethod = requestMethod;
        return this;
    }

    public ApiCaller setUrl(String url){
        this.url = url;
        return this;
    }

    public ApiCaller setQueryParams(Map<String, String> queryParams){
        if (this.url.isEmpty()) {
            return this;
        }

        String queryString = new String();
        int paramCnt = queryParams.entrySet().size();

        if (queryParams != null && paramCnt > 0) {
            queryString += "?";
            Iterator<Map.Entry<String, String>> iter = queryParams.entrySet().iterator();

            while (iter.hasNext()) {
                Map.Entry mapEntry = iter.next();
                queryString = queryString + mapEntry.getKey() + "=" + mapEntry.getValue();
                if (iter.hasNext())
                    queryString += "&";
            }

            setUrl(this.url.concat(queryString));
        }

        return this;
    }

    public ApiCaller setConnectionTimeout(Integer connectionTimeout){
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public ApiCaller setReadTimeout(Integer readTimeout){
        this.readTimeout = readTimeout;
        return this;
    }

    public ApiCaller setAuth(String authToken){
        headers.put("Authorization", authToken);
        return this;
    }

    public ApiCaller setContentType(String contentType){
        headers.put("Content-Type", contentType);
        return this;
    }

    public ApiCaller setData(String data){
        this.data = data;
        return this;
    }

    public ApiCallerResponse call(){
        URL url = null;
        try {
            url = new URL(this.url);
            HttpURLConnection conn = null;
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(this.requestMethod.name());
            conn.setConnectTimeout(this.connectionTimeout);
            conn.setReadTimeout(this.readTimeout);

            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    conn.setRequestProperty(header.getKey(), header.getValue());
                }
            }

            if (data != null) {
                byte[] postDataBytes = data.toString().getBytes();
                conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                conn.setDoOutput(true);
                conn.getOutputStream().write(postDataBytes);

            }

            BufferedReader rd = null;

            if(conn.getResponseCode() == 200 || conn.getResponseCode() == 201){
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            }
            else{
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            StringBuilder result = new StringBuilder();

            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();


            ApiCallerResponse apiCallerResponse = new ApiCallerResponse();
            apiCallerResponse.setResponseCode(conn.getResponseCode());
            apiCallerResponse.setResponseMsg(result.toString());
            return apiCallerResponse;

        } catch (Exception e) {

        }
        return null;
    }
}
