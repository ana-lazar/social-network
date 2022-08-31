package repositories.interfaces;

import domain.entities.Friendship;
import domain.Tuple;
import repositories.interfaces.Repository;

import java.sql.SQLException;

public interface FriendshipRepository extends Repository<Tuple<Long, Long>, Friendship> {
    void removeAll(Long userId) throws SQLException;
}
