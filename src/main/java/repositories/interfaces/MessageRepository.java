package repositories.interfaces;

import domain.entities.Message;
import repositories.interfaces.Repository;

import java.sql.SQLException;

public interface MessageRepository extends Repository<Long, Message> {
    void removeAll(Long userId) throws SQLException;

    void removeAllFrom(Long firstId, Long secondId) throws SQLException;

    default Long findId() throws SQLException {
        Long id = 1L;
        while (findOne(id).isPresent()) {
            id++;
        }
        return id;
    }
}
