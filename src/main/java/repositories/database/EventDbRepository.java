package repositories.database;

import domain.Participant;
import domain.entities.Event;
import domain.entities.User;
import domain.exceptions.ValidationException;
import domain.validators.Validator;
import repositories.interfaces.EventRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class EventDbRepository extends AbstractDbRepository<Long, Event> implements EventRepository {
    public EventDbRepository(String tableName, Validator<Event> validator) throws SQLException {
        super(tableName, validator);
    }

    @Override
    protected Event extractEntity(ResultSet resultSet) throws SQLException {
        Event event = new Event();
        event.setId(resultSet.getLong("Id"));
        event.setTitle(resultSet.getString("Title"));
        event.setDescription(resultSet.getString("Description"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        event.setDate(LocalDate.parse(resultSet.getString("Date"), formatter));
        PreparedStatement statement = connection.prepareStatement(
                "SELECT * " +
                        "FROM \"Participants\" \"P\" INNER JOIN \"Users\" \"U\" ON \"P\".\"UserId\" = \"U\".\"Id\" " +
                        "INNER JOIN \"Events\" \"E\" ON \"E\".\"Id\" = \"P\".\"EventId\" " +
                        "WHERE \"E\".\"Id\" = ?;"
        );
        statement.setLong(1, event.getId());
        ResultSet set = statement.executeQuery();
        List<Participant> participants = new ArrayList<>();
        while (set.next()) {
            User user = new User();
            user.setId(set.getLong("UserId"));
            user.setFirstName(set.getString("FirstName"));
            user.setLastName(set.getString("SecondName"));
            user.setUserName(set.getString("UserName"));
            user.setPassword(set.getString("Password"));
            Participant participant = new Participant(event.getId(), user, set.getString("Notifications").equals("ON"));
            participants.add(participant);
        }
        event.setParticipants(participants);
        return event;
    }

    @Override
    protected ResultSet findSet(Long id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM \"" + tableName + "\" WHERE \"Id\" = ?;");
        statement.setLong(1, id);
        return statement.executeQuery();
    }

    @Override
    protected void writeData(Event event) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO \"" + tableName + "\" (\"Id\", \"Title\", \"Description\", \"Date\") VALUES (?, ?, ?, ?);");
        statement.setLong(1, event.getId());
        statement.setString(2, event.getTitle());
        statement.setString(3, event.getDescription());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        statement.setString(4, event.getDate().format(formatter));
        statement.execute();
        statement.close();
        for (Participant participant : event.getParticipants()) {
            PreparedStatement partStatement = connection.prepareStatement("INSERT INTO \"Participants\" (\"EventId\", \"UserId\", \"Notifications\") VALUES (?, ?, ?)");
            partStatement.setLong(1, event.getId());
            partStatement.setLong(2, participant.getUser().getId());
            partStatement.setString(3, participant.getNotifications().toString());
            partStatement.execute();
            partStatement.close();
        }
    }

    @Override
    protected void updateRow(Event event) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE \"Events\" SET \"Title\" = ?, \"Description\" = ?, \"Date\" = ? WHERE \"Id\" = ?");
        statement.setString(1, event.getTitle());
        statement.setString(2, event.getDescription());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        statement.setString(3, event.getDate().format(formatter));
        statement.setLong(4, event.getId());
        statement.execute();
        statement.close();
    }

    @Override
    protected void deleteRow(Long id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM \"Participants\" WHERE \"EventId\" = ?");
        statement.setLong(1, id);
        statement.execute();
        statement.close();
        statement = connection.prepareStatement("DELETE FROM \"Events\" WHERE \"Id\" = ?");
        statement.setLong(1, id);
        statement.execute();
        statement.close();
    }

    public void removeAllFrom(Long id) throws SQLException {
        List<Event> events = new ArrayList<>((Collection<? extends Event>) findAll());
        for (Event event : events) {
            if (event.getParticipants().stream().anyMatch(p -> p.getUser().getId().equals(id))) {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM \"Participants\" WHERE \"EventId\" = ? AND \"UserId\" = ?;");
                statement.setLong(1, event.getId());
                statement.setLong(2, id);
                statement.execute();
                statement.close();
            }
        }
    }

    @Override
    public void addParticipant(User user, Event event) throws SQLException {
        event.addParticipant(new Participant(event.getId(), user, true));
        PreparedStatement partStatement = connection.prepareStatement("INSERT INTO \"Participants\" (\"EventId\", \"UserId\", \"Notifications\") VALUES (?, ?, ?)");
        partStatement.setLong(1, event.getId());
        partStatement.setLong(2, user.getId());
        partStatement.setString(3, "ON");
        partStatement.execute();
        partStatement.close();
    }

    @Override
    public void removeParticipant(User user, Event event) throws SQLException {
        event.removeParticipant(new Participant(event.getId(), user, true));
        PreparedStatement partStatement = connection.prepareStatement("DELETE FROM \"Participants\" WHERE \"UserId\" = ? AND \"EventId\" = ?;");
        partStatement.setLong(1, user.getId());
        partStatement.setLong(2, event.getId());
        partStatement.execute();
        partStatement.close();
    }

    @Override
    public void dismissNotifications(User user, Event event) throws SQLException {
        event.dismissFrom(user);
        PreparedStatement partStatement = connection.prepareStatement("UPDATE \"Participants\" SET \"Notifications\" = ? WHERE \"UserId\" = ? AND \"EventId\" = ?;");
        partStatement.setString(1, "OFF");
        partStatement.setLong(2, user.getId());
        partStatement.setLong(3, event.getId());
        partStatement.execute();
        partStatement.close();
    }

    @Override
    public Optional<Event> save(Event entity) throws ValidationException, IllegalArgumentException, SQLException {
        entity.setId(findId());
        return super.save(entity);
    }
}
