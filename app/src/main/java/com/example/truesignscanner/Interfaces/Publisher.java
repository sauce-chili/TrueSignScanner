package com.example.truesignscanner.Interfaces;

public interface Publisher<T> {

    void subscribe(Subscriber<T> sub);

    void unsubscribe(Subscriber<T> sub);

    void notifyDataChange();
}
