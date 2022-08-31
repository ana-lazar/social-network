package repositories.file;

import domain.entities.Entity;
import domain.exceptions.ValidationException;
import domain.validators.Validator;
import repositories.memory.MemoryRepository;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractFileRepository<ID, E extends Entity<ID>> extends MemoryRepository<ID, E> {
    String fileName;

    public AbstractFileRepository(String fileName, Validator<E> validator) {
        super(validator);
        this.fileName = fileName;
        loadData();
    }

    @Override
    public Optional<E> save(E entity) throws ValidationException, IllegalArgumentException, SQLException {
        Optional<E> optional = super.save(entity);
        if (optional.isEmpty()) {
            writeToFile(entity);
        }
        return optional;
    }

    @Override
    public Optional<E> modify(E entity) throws ValidationException, IllegalArgumentException, SQLException {
        Optional<E> optional = super.modify(entity);
        if (optional.isEmpty()) {
            reWriteFile();
        }
        return optional;
    }

    @Override
    public Optional<E> remove(ID id) throws ValidationException, IllegalArgumentException {
        Optional<E> optional = super.remove(id);
        optional.ifPresent(value -> reWriteFile());
        return optional;
    }

    protected void loadData() {
        try {
            Path path = Paths.get(fileName);
            List<String> lines = Files.readAllLines(path);
            lines.forEach(line -> {
                E entity = null;
                try {
                    entity = extractEntity(Arrays.asList(line.split(",")));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                try {
                    super.save(entity);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            });
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void reWriteFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false))) {
            // Empties the file
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        for (Map.Entry<ID, E> entity : entities.entrySet()) {
            writeToFile(entity.getValue());
        }
    }

    private void writeToFile(E entity) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(entityToString(entity));
            writer.newLine();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public abstract E extractEntity(List<String> attributes) throws ParseException;

    protected abstract String entityToString(E entity);
}
