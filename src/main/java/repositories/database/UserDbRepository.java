package repositories.database;

import domain.entities.User;
import domain.exceptions.DuplicatedIdException;
import domain.exceptions.DuplicatedUserNameException;
import domain.exceptions.EmptyFieldsException;
import domain.exceptions.ValidationException;
import domain.validators.Validator;
import repositories.interfaces.UserRepository;
import repositories.file.UserFileRepository;

import java.sql.*;
import java.util.*;

public class UserDbRepository extends AbstractDbRepository<Long, User> implements UserRepository {
    public UserDbRepository(String tableName, Validator<User> validator) throws SQLException {
        super(tableName, validator);
    }

    @Override
    protected User extractEntity(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("Id");
        String firstName = resultSet.getString("FirstName");
        String lastName = resultSet.getString("SecondName");
        String userName = resultSet.getString("UserName");
        String password = resultSet.getString("Password");
        User user = new User(firstName, lastName, userName, password);
        user.setId(id);
        return user;
    }

    @Override
    protected ResultSet findSet(Long id) throws IllegalArgumentException, SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM \"" + tableName + "\" WHERE \"Id\" = ?;");
        statement.setLong(1, id);
        return statement.executeQuery();
    }

    @Override
    protected void writeData(User user) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO \"Users\" (\"Id\", \"FirstName\", \"SecondName\", \"UserName\", \"Password\") VALUES (?, ?, ?, ?, ?)");
        statement.setLong(1, user.getId());
        statement.setString(2, user.getFirstName());
        statement.setString(3, user.getLastName());
        statement.setString(4, user.getUserName());
        statement.setString(5, user.getPassword());
        statement.execute();
    }

    @Override
    protected void updateRow(User user) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE \"Users\" SET \"FirstName\" = ?, \"SecondName\" = ?, \"Password\" = ? WHERE \"Id\" = ?");
        statement.setString(1, user.getFirstName());
        statement.setString(2, user.getLastName());
        statement.setString(3, user.getPassword());
        statement.setLong(4, user.getId());
        statement.execute();
    }

    @Override
    protected void deleteRow(Long id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM \"Users\" WHERE \"Id\" = ?");
        statement.setLong(1, id);
        statement.execute();
    }

    @Override
    public Optional<User> findByUserName(String userName) throws SQLException {
        if (userName == null) {
            throw new EmptyFieldsException();
        }
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM \"" + tableName + "\" WHERE \"UserName\" = ?;");
        statement.setString(1, userName);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return Optional.of(extractEntity(resultSet));
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> save(User user) throws ValidationException, IllegalArgumentException, SQLException {
        if (user == null) {
            throw new EmptyFieldsException();
        }
        findOne(user.getId()).ifPresent(value -> { throw new DuplicatedIdException(); });
        findByUserName(user.getUserName()).ifPresent(value -> { throw new DuplicatedUserNameException(); });
        validator.validate(user);
        writeData(user);
        return Optional.empty();
    }

    public int findCommunities(Map<Long, User> users) {
        UserFileRepository.UserGraph graph = new UserFileRepository.UserGraph(users);
        return graph.noOfComponents();
    }

    public List<User> findLargest(Map<Long, User> users) {
        UserFileRepository.UserGraph graph = new UserFileRepository.UserGraph(users);
        return graph.largestComm();
    }

    static class UserGraph {
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
