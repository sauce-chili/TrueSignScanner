package com.example.truesignscanner.Interfaces;

public interface Subscriber<T> {
    void handleChanges(T data);
}