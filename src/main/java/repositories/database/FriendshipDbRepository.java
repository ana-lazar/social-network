package repositories.database;

import domain.entities.Friendship;
import domain.Tuple;
import domain.validators.Validator;
import repositories.interfaces.FriendshipRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FriendshipDbRepository extends AbstractDbRepository<Tuple<Long, Long>, Friendship> implements FriendshipRepository {
    public FriendshipDbRepository(String tableName, Validator<Friendship> validator) throws SQLException {
        super(tableName, validator);
    }

    @Override
    protected Friendship extractEntity(ResultSet resultSet) throws SQLException {
        Long firstId = resultSet.getLong("FirstId");
        Long secondId = resultSet.getLong("SecondId");
        String date = resultSet.getString("DateF");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        Friendship friendship = new Friendship(LocalDateTime.parse(date, formatter));
        friendship.setId(new Tuple<>(firstId, secondId));
        return friendship;
    }

    @Override
    protected ResultSet findSet(Tuple<Long, Long> id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM \"" + tableName + "\" WHERE \"FirstId\" = ? AND \"SecondId\" = ? OR \"FirstId\" = ? AND \"SecondId\" = ?;");
        statement.setLong(1, id.getLeft());
        statement.setLong(2, id.getRight());
        statement.setLong(3, id.getRight());
        statement.setLong(4, id.getLeft());
        return statement.executeQuery();
    }

    @Override
    protected void writeData(Friendship friendship) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO \"Friendships\" (\"FirstId\", \"SecondId\", \"DateF\") VALUES (?, ?, ?)");
        statement.setLong(1, friendship.getId().getLeft());
        statement.setLong(2, friendship.getId().getRight());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        statement.setString(3, friendship.getDate().format(formatter));
        statement.execute();
    }

    @Override
    protected void updateRow(Friendship friendship) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE \"Friendships\" SET \"DateF\" = ? WHERE \"FirstId\" = ? AND \"SecondId\" = ?;");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        statement.setString(1, friendship.getDate().format(formatter));
        statement.setLong(2, friendship.getId().getLeft());
        statement.setLong(3, friendship.getId().getRight());
        statement.execute();
    }

    @Override
    protected void deleteRow(Tuple<Long, Long> id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM \"Friendships\" WHERE \"FirstId\" = ? AND \"SecondId\" = ? OR \"FirstId\" = ? AND \"SecondId\" = ?;");
        statement.setLong(1, id.getLeft());
        statement.setLong(2, id.getRight());
        statement.setLong(3, id.getRight());
        statement.setLong(4, id.getLeft());
        statement.execute();
    }

    @Override
    public void removeAll(Long userId) throws SQLException {
        List<Friendship> friendships = new ArrayList<Friendship>((Collection<? extends Friendship>) findAll());
        for (Friendship friendship : friendships) {
            if (friendship.getId().getRight().equals(userId) || friendship.getId().getLeft().equals(userId)) {
                this.remove(friendship.getId());
            }
        }
    }
}
