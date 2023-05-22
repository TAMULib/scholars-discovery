package edu.tamu.scholars.middleware.messaging;

public abstract class ReadEntityMessage<E> implements EntityMessage {

    private final E entity;

    public ReadEntityMessage(E entity) {
        this.entity = entity;
    }

    public E getEntity() {
        return entity;
    }

}
