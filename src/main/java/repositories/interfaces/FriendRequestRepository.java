package repositories.interfaces;

import domain.Tuple;
import domain.entities.FriendRequest;
import repositories.interfaces.Repository;

import java.sql.SQLException;

public interface FriendRequestRepository extends Repository<Tuple<Long, Long>, FriendRequest> {
    void removeAll(Long userId) throws SQLException;

    void removeAllFrom(Long firstId, Long secondId) throws SQLException;
}
