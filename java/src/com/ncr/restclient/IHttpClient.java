package com.ncr.restclient;

import com.sun.jersey.api.client.ClientResponse;
        import javax.ws.rs.core.MultivaluedMap;

public interface IHttpClient {
    public ClientResponse get(MultivaluedMap<String,String> params);
    public ClientResponse put(MultivaluedMap<String,String> params);
    public ClientResponse post(MultivaluedMap<String,String> params);
    public ClientResponse post(MultivaluedMap<String,String> params, String content);
    public ClientResponse post(MultivaluedMap<String,String> params, String content,String accept, String type,String timeout);
    public ClientResponse post(MultivaluedMap<String,String> params, String content,String username, String password);
    public ClientResponse delete(MultivaluedMap<String,String> params);
}