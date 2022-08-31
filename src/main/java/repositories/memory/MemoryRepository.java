package repositories.memory;

import domain.entities.Entity;
import domain.exceptions.DuplicatedIdException;
import domain.exceptions.EmptyFieldsException;
import domain.exceptions.ValidationException;
import domain.validators.Validator;
import repositories.interfaces.Repository;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MemoryRepository<ID, E extends Entity<ID>> implements Repository<ID, E> {
    private final Validator<E> validator;
    protected Map<ID, E> entities;

    public MemoryRepository(Validator<E> validator) {
        this.validator = validator;
        entities = new HashMap<>();
    }

    @Override
    public Optional<E> findOne(ID id) throws IllegalArgumentException {
        if (id == null) {
            throw new EmptyFieldsException();
        }
        return Optional.ofNullable(entities.get(id));
    }

    @Override
    public Iterable<E> findAll() {
        return entities.values();
    }

    @Override
    public Optional<E> save(E entity) throws ValidationException, IllegalArgumentException, SQLException {
        if (entity == null) {
            throw new EmptyFieldsException();
        }
        if (entities.containsKey(entity.getId())) {
            throw new DuplicatedIdException();
        }
        validator.validate(entity);
        return Optional.ofNullable(entities.putIfAbsent(entity.getId(), entity));
    }

    @Override
    public Optional<E> remove(ID id) throws IllegalArgumentException {
        if (id == null) {
            throw new EmptyFieldsException();
        }
        return Optional.ofNullable(entities.remove(id));
    }

    @Override
    public Optional<E> modify(E entity) throws ValidationException, IllegalArgumentException, SQLException {
        if (entity == null) {
            throw new EmptyFieldsException();
        }
        validator.validate(entity);
        if (entities.containsKey(entity.getId())) {
            entities.replace(entity.getId(), entity);
            return Optional.empty();
        }
        return Optional.of(entity);
    }
}
