package repositories.database;

import config.DatabaseProperties;
import domain.entities.Entity;
import domain.exceptions.DuplicatedIdException;
import domain.exceptions.EmptyFieldsException;
import domain.exceptions.InexistentIdException;
import domain.exceptions.ValidationException;
import domain.validators.Validator;
import repositories.interfaces.Repository;
import repositories.memory.MemoryRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractDbRepository<ID, E extends Entity<ID>> implements Repository<ID, E> {
    protected final Validator<E> validator;
    protected final Connection connection;
    protected final String tableName;

    public Connection connect(String url, String user, String password) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return connection;
    }

    public AbstractDbRepository(String tableName, Validator<E> validator) throws SQLException {
        this.validator = validator;
        this.tableName = tableName;
        connection = connect(DatabaseProperties.getUrl(), DatabaseProperties.getUser(), DatabaseProperties.getPassword());
    }

    @Override
    public Optional<E> findOne(ID id) throws IllegalArgumentException, SQLException {
        if (id == null) {
            throw new EmptyFieldsException();
        }
        Optional<E> optional = Optional.empty();
        ResultSet resultSet = findSet(id);
        if (resultSet.next()) {
            optional = Optional.of(extractEntity(resultSet));
        }
        return optional;
    }

    protected abstract ResultSet findSet(ID id) throws SQLException;

    protected abstract E extractEntity(ResultSet resultSet) throws SQLException;

    @Override
    public Iterable<E> findAll() throws SQLException {
        List<E> entities = new ArrayList<E>();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM \"" + tableName + "\";");
        while (resultSet.next()) {
            E entity = extractEntity(resultSet);
            entities.add(entity);
        }
        return entities;
    }

    @Override
    public Optional<E> save(E entity) throws ValidationException, IllegalArgumentException, SQLException {
        if (entity == null) {
            throw new EmptyFieldsException();
        }
        findOne(entity.getId()).ifPresent(e -> { throw new DuplicatedIdException(); });
        validator.validate(entity);
        writeData(entity);
        return Optional.empty();
    }

    protected abstract void writeData(E entity) throws SQLException;

    @Override
    public Optional<E> modify(E entity) throws ValidationException, IllegalArgumentException, SQLException {
        if (entity == null) {
            throw new EmptyFieldsException();
        }
        validator.validate(entity);
        if (findOne(entity.getId()).isPresent()) {
            updateRow(entity);
            return Optional.empty();
        }
        return Optional.of(entity);
    }

    protected abstract void updateRow(E entity) throws SQLException;

    @Override
    public Optional<E> remove(ID id) throws ValidationException, IllegalArgumentException, SQLException {
        if (id == null) {
            throw new EmptyFieldsException();
        }
        Optional<E> entity = findOne(id);
        deleteRow(id);
        return entity;
    }

    protected abstract void deleteRow(ID id) throws SQLException;
}
