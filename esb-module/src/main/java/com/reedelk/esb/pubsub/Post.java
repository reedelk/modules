package com.reedelk.esb.pubsub;

abstract class Post<T> {

    private T message;

    protected Post(T message) {
        this.message = message;
    }

    public T getMessage() {
        return message;
    }
}