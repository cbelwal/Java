package com.dasmic.android.libhttpclient;

import org.junit.Assert;
import org.junit.Test;

public class HttpClientGetTest {
    @Test
    public void SimpleGetTest() {
        HttpClient httpClient = new HttpClient("http://www.google.com");
        String value = httpClient.Get();
        Assert.assertNotNull(value);
    }

    @Test
    public void InvalueURLGetTest() {
        HttpClient httpClient = new HttpClient("http://192.168.1.1");
        String value = httpClient.Get();
        Assert.assertNotNull(value);
    }
}
