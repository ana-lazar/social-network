package repositories.file;

import domain.entities.User;
import domain.exceptions.ValidationException;
import domain.validators.Validator;
import repositories.interfaces.UserRepository;

import java.sql.SQLException;
import java.util.*;

public class UserFileRepository extends AbstractFileRepository<Long, User> implements UserRepository {
    public UserFileRepository(String fileName, Validator<User> validator) {
        super(fileName, validator);
    }

    @Override
    public User extractEntity(List<String> args) {
        User user = new User(args.get(1), args.get(2), args.get(3), args.get(4));
        user.setId(Long.parseLong(args.get(0)));
        return user;
    }

    @Override
    protected String entityToString(User entity) {
        return entity.toString();
    }

    public void addFriendship(Long firstId, Long secondId) {
        User firstUser = entities.get(firstId);
        User secondUser = entities.get(secondId);
        firstUser.addFriend(secondUser);
        secondUser.addFriend(firstUser);
    }

    public void removeFriendship(Long firstId, Long secondId) {
        User firstUser = entities.get(firstId);
        User secondUser = entities.get(secondId);
        firstUser.removeFriend(secondId);
        secondUser.removeFriend(firstId);
    }

    @Override
    public int findCommunities(Map<Long, User> usersMap) {
        UserGraph graph = new UserGraph(entities);
        return graph.noOfComponents();
    }

    @Override
    public List<User> findLargest(Map<Long, User> usersMap) {
        UserGraph graph = new UserGraph(entities);
        return graph.largestComm();
    }

    @Override
    public Optional<User> findByUserName(String userName) throws SQLException {
        return Optional.empty();
    }

    public void removeFriendships(Long id) {
        List<User> friends = entities.get(id).getFriends();
        for (User user : friends) {
            this.removeFriendship(id, user.getId());
        }
    }

    @Override
    public Optional<User> modify(User entity) throws ValidationException, IllegalArgumentException, SQLException {
        if (entity == null) {
            throw new IllegalArgumentException("Entity must be not null");
        }
        if (entities.containsKey(entity.getId())) {
            entity.setFriends(entities.get(entity.getId()).getFriends());
        }

        return super.modify(entity);
    }

    public static class UserGraph {
        private final Map<Long, User> users;
        private Map<Long, Boolean> visited;
        private List<User> maxList;

        public UserGraph(Map<Long, User> users) {
            this.users = users;
            this.visited = new HashMap<>();
            users.forEach((key, value) -> visited.put(value.getId(), false));
        }

        public void DFS(Long userId) {
            visited.replace(userId, true);
            List<User> friends = users.get(userId).getFriends();
            for (User friend : friends) {
                if (!visited.get(friend.getId())) {
                    DFS(friend.getId());
                }
            }
        }

        public int noOfComponents() {
            int components = 0;
            for (User user : users.values()) {
                if (!visited.get(user.getId())) {
                    components++;
                    DFS(user.getId());
                }
            }
            return components;
        }

        private void findPath(Long userId, List<User> auxList) {
            auxList.add(users.get(userId));
            visited.replace(userId, true);
            List<User> friends = users.get(userId).getFriends();
            for (User friend : friends) {
                if (!visited.get(friend.getId())) {
                    findPath(friend.getId(), auxList);
                }
            }
            if (auxList.size() > maxList.size()) {
                maxList = auxList;
            }
        }

        public List<User> largestComm() {
            maxList = new ArrayList<>();
            for (User user : users.values()) {
                List<User> auxList = new ArrayList<>();
                users.forEach((key, value) -> visited.put(value.getId(), false));
                findPath(user.getId(), auxList);
            }
            return maxList;
        }
    }
}
