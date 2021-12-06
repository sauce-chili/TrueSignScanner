package com.example.truesignscanner.Interfaces;

public interface Publisher<T> {

    T getData();

    void subscribe(Subscriber<T> sub);

    void unsubscribe(Subscriber<T> sub);

    void notifyDataChange();

    void setData(T d);

}
