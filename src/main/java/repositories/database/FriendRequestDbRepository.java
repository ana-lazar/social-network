package repositories.database;

import domain.Status;
import domain.Tuple;
import domain.entities.FriendRequest;
import domain.validators.Validator;
import repositories.interfaces.FriendRequestRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FriendRequestDbRepository extends AbstractDbRepository<Tuple<Long, Long>, FriendRequest> implements FriendRequestRepository {
    public FriendRequestDbRepository(String tableName, Validator<FriendRequest> validator) throws SQLException {
        super(tableName, validator);
    }

    @Override
    protected FriendRequest extractEntity(ResultSet resultSet) throws SQLException {
        String date = resultSet.getString("Date");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setDate(LocalDateTime.parse(date, formatter));
        friendRequest.setId(new Tuple<>(resultSet.getLong("From"), resultSet.getLong("To")));
        friendRequest.setStatus(Status.valueOf(resultSet.getString("Status")));
        return friendRequest;
    }

    @Override
    protected ResultSet findSet(Tuple<Long, Long> id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM \"" + tableName + "\" WHERE \"From\" = ? AND \"To\" = ?;");
        statement.setLong(1, id.getLeft());
        statement.setLong(2, id.getRight());
        return statement.executeQuery();
    }

    @Override
    protected void writeData(FriendRequest entity) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO \"FriendRequests\" (\"From\", \"To\", \"Status\", \"Date\") VALUES (?, ?, ?, ?)");
        statement.setLong(1, entity.getFrom().getId());
        statement.setLong(2, entity.getTo().getId());
        statement.setString(3, entity.getStatus());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        statement.setString(4, entity.getDate().format(formatter));
        statement.execute();
        statement.close();
    }

    @Override
    protected void updateRow(FriendRequest entity) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE \"FriendRequests\" SET \"Status\" = ? WHERE \"From\" = ? AND \"To\" = ?");
        statement.setString(1, entity.getStatus());
        statement.setLong(2, entity.getFrom().getId());
        statement.setLong(3, entity.getTo().getId());
        statement.execute();
        statement.close();
    }

    @Override
    protected void deleteRow(Tuple<Long, Long> id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM \"FriendRequests\" WHERE \"From\" = ? AND \"To\" = ?");
        statement.setLong(1, id.getLeft());
        statement.setLong(2, id.getRight());
        statement.execute();
        statement.close();
    }

    @Override
    public void removeAll(Long userId) throws SQLException {
        List<FriendRequest> friendRequests = new ArrayList<FriendRequest>((Collection<? extends FriendRequest>) findAll());
        for (FriendRequest friendRequest : friendRequests) {
            if (friendRequest.getId().getRight().equals(userId) || friendRequest.getId().getLeft().equals(userId)) {
                this.remove(friendRequest.getId());
            }
        }
    }

    @Override
    public void removeAllFrom(Long firstId, Long secondId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM \"FriendRequests\" WHERE \"From\" = ? AND \"To\" = ?");
        statement.setLong(1, firstId);
        statement.setLong(2, secondId);
        statement.execute();
        statement.close();
    }
}
