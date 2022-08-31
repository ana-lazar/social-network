package utils;

public class DomainEvent<E> implements MainEvent {
    private final DomainEventType type;
    private final E entity;
    private E oldEntity;

    public DomainEvent(DomainEventType type, E entity) {
        this.type = type;
        this.entity = entity;
    }
    public DomainEvent(DomainEventType type, E entity, E oldEntity) {
        this.type = type;
        this.entity = entity;
        this.oldEntity = oldEntity;
    }

    public DomainEventType getType() {
        return type;
    }

    public E getEntity() {
        return entity;
    }

    public E getOldEntity() {
        return oldEntity;
    }
}
