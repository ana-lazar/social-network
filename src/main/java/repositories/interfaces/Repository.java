package repositories.interfaces;

import domain.entities.Entity;
import domain.exceptions.DuplicatedIdException;
import domain.exceptions.ValidationException;

import java.sql.SQLException;
import java.util.Optional;

public interface Repository<ID, E extends Entity<ID>> {
    Optional<E> findOne(ID id) throws IllegalArgumentException, SQLException;

    Iterable<E> findAll() throws SQLException;

    Optional<E> save(E entity) throws ValidationException, IllegalArgumentException, DuplicatedIdException, SQLException;

    Optional<E> remove(ID id) throws IllegalArgumentException, SQLException;

    Optional<E> modify(E entity) throws ValidationException, IllegalArgumentException, SQLException;
}
