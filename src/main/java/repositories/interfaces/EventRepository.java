package repositories.interfaces;

import domain.entities.Event;
import domain.entities.User;

import java.sql.SQLException;

public interface EventRepository extends Repository<Long, Event> {
    void removeAllFrom(Long id) throws SQLException;

    void addParticipant(User user, Event event) throws SQLException;

    void removeParticipant(User user, Event e) throws SQLException;

    void dismissNotifications(User user, Event event) throws SQLException;

    default Long findId() throws SQLException {
        Long id = 1L;
        while (findOne(id).isPresent()) {
            id++;
        }
        return id;
    }
}
