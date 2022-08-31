package repositories.database;

import domain.entities.Message;
import domain.entities.User;
import domain.exceptions.ValidationException;
import domain.validators.Validator;
import repositories.interfaces.MessageRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class MessageDbRepository extends AbstractDbRepository<Long, Message> implements MessageRepository {
    public MessageDbRepository(String tableName, Validator<Message> validator) throws SQLException {
        super(tableName, validator);
    }

    @Override
    protected Message extractEntity(ResultSet resultSet) throws SQLException {
        User from = new User();
        from.setId(resultSet.getLong("From"));
        PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM \"Users\" WHERE \"Users\".\"Id\" = ?"
        );
        statement.setLong(1, from.getId());
        ResultSet fromSet = statement.executeQuery();
        fromSet.next();
        from.setFirstName(fromSet.getString("FirstName"));
        from.setLastName(fromSet.getString("SecondName"));
        from.setUserName(fromSet.getString("UserName"));
        from.setPassword(fromSet.getString("Password"));
        Message message = new Message(from, resultSet.getString("Text"));
        message.setId(resultSet.getLong("Id"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        message.setDate(LocalDateTime.parse(resultSet.getString("Date"), formatter));
        statement = connection.prepareStatement(
                "SELECT * " +
                        "FROM \"Recievers\" \"R\" INNER JOIN \"Messages\" \"M\" ON \"R\".\"MessageId\" = \"M\".\"Id\" " +
                        "INNER JOIN \"Users\" \"U\" ON \"U\".\"Id\" = \"R\".\"UserId\" " +
                        "WHERE \"M\".\"Id\" = ?;"
        );
        statement.setLong(1, message.getId());
        ResultSet toSet = statement.executeQuery();
        List<User> toList = new ArrayList<>();
        while (toSet.next()) {
            User user = new User();
            user.setId(toSet.getLong("UserId"));
            user.setFirstName(toSet.getString("FirstName"));
            user.setLastName(toSet.getString("SecondName"));
            user.setUserName(fromSet.getString("UserName"));
            user.setPassword(fromSet.getString("Password"));
            toList.add(user);
        }
        message.setTo(toList);
        if (resultSet.getLong("Reply") != -1) {
            Message reply = new Message();
            reply.setId(resultSet.getLong("Reply"));
            message.setReply(reply);
        }
        return message;
    }

    @Override
    protected ResultSet findSet(Long id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM \"" + tableName + "\" WHERE \"Id\" = ?;");
        statement.setLong(1, id);
        return statement.executeQuery();
    }

    @Override
    protected void updateRow(Message entity) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE \"Messages\" SET \"From\" = ?, \"Text\" = ?, \"Date\" = ? WHERE \"Id\" = ?");
        statement.setLong(1, entity.getFrom().getId());
        statement.setString(2, entity.getText());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        statement.setString(3, entity.getDate().format(formatter));
        statement.setLong(4, entity.getId());
        statement.execute();
        statement.close();
    }

    @Override
    protected void deleteRow(Long id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM \"Recievers\" WHERE \"MessageId\" = ?");
        statement.setLong(1, id);
        statement.execute();
        statement.close();
        statement = connection.prepareStatement("DELETE FROM \"Messages\" WHERE \"Id\" = ?");
        statement.setLong(1, id);
        statement.execute();
        statement.close();
    }

    @Override
    protected void writeData(Message message) throws SQLException {
        PreparedStatement messageStatement = connection.prepareStatement("INSERT INTO \"Messages\" (\"Id\", \"From\", \"Text\", \"Date\", \"Reply\") VALUES (?, ?, ?, ?, ?)");
        messageStatement.setLong(1, message.getId());
        messageStatement.setLong(2, message.getFrom().getId());
        messageStatement.setString(3, message.getText());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        messageStatement.setString(4, message.getDate().format(formatter));
        if (message.getReply() != null) {
            messageStatement.setLong(5, message.getReply().getId());
        }
        else {
            messageStatement.setLong(5, -1);
        }
        messageStatement.execute();
        messageStatement.close();
        for (User user : message.getTo()) {
            PreparedStatement recieveStatement = connection.prepareStatement("INSERT INTO \"Recievers\" (\"MessageId\", \"UserId\") VALUES (?, ?)");
            recieveStatement.setLong(1, message.getId());
            recieveStatement.setLong(2, user.getId());
            recieveStatement.execute();
            recieveStatement.close();
        }
    }

    @Override
    public void removeAll(Long userId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM \"Recievers\" WHERE \"UserId\" = ?");
        statement.setLong(1, userId);
        statement.execute();
        statement.close();
        List<Message> messages = new ArrayList<Message>((Collection<? extends Message>) findAll());
        for (Message message : messages) {
            if (message.getFrom().getId().equals(userId)) {
                this.remove(message.getId());
            }
            else if (message.getTo().stream().anyMatch(value -> value.getId().equals(userId))) {
                if (message.getTo().size() > 1) {
                    message.getTo().removeIf(value -> value.getId().equals(userId));
                }
                else {
                    this.remove(message.getId());
                }
            }
        }
    }

    @Override
    public Optional<Message> save(Message entity) throws ValidationException, IllegalArgumentException, SQLException {
        entity.setId(findId());
        return super.save(entity);
    }

    public List<Message> getChat(Long from, Long to) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "SELECT * " +
                        "FROM \"Messages\" \"M\" INNER JOIN \"Recievers\" \"R\" ON \"R\".\"MessageId\" = \"M\".\"Id\" " +
                        "WHERE (\"M\".\"From\" = ? AND \"R\".\"UserId\" = ?) OR (\"M\".\"From\" = ? AND \"R\".\"UserId\" = ?);");
        statement.setLong(1, from);
        statement.setLong(2, to);
        statement.setLong(3, to);
        statement.setLong(4, from);
        ResultSet resultSet = statement.executeQuery();
        List<Message> messages = new ArrayList<>();
        while (resultSet.next()) {
            messages.add(extractEntity(resultSet));
        }
        statement.close();
        return messages;
    }

    public void removeAllFrom(Long firstId, Long secondId) throws SQLException {
        List<Message> messages = new ArrayList<Message>((Collection<? extends Message>) findAll());
        for (Message message : messages) {
            if (message.getFrom().getId().equals(firstId)) {
                if (message.getTo().stream().anyMatch(value -> value.getId().equals(secondId))) {
                    PreparedStatement statement = connection.prepareStatement("DELETE FROM \"Recievers\" WHERE \"MessageId\" = ? AND \"UserId\" = ?;");
                    statement.setLong(1, message.getId());
                    statement.setLong(2, secondId);
                    statement.execute();
                    statement.close();
                    if (message.getTo().size() <= 1) {
                        this.remove(message.getId());
                    }
                }
            }
        }
    }
}
