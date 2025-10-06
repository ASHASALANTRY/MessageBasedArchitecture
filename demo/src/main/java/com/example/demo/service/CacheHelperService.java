package com.example.demo.service;

public interface CacheHelperService {
    void addMessageStatusToCache(String key, String sentForProcessing);
    String getDataFromCache(String key);
}
