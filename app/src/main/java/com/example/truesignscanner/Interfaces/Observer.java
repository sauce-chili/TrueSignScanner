package com.example.truesignscanner.Interfaces;

public interface Observer<T> {

    interface Publisher{
        void subscribe(Subscriber sub);
        void unsubscribe(Subscriber sub);
        void notifyDataChange();
    }

    interface Subscriber{
        void handleNotify();
    }
}
