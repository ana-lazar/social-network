package repositories.interfaces;

import domain.entities.User;

import java.sql.SQLException;
import java.util.*;

public interface UserRepository extends Repository<Long, User> {
    int findCommunities(Map<Long, User> usersMap);

    List<User> findLargest(Map<Long, User> usersMap);

    Optional<User> findByUserName(String userName) throws SQLException;
}
