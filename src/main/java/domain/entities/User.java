package domain.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class User extends Entity<Long> {
    private String userName;
    private String password;
    private String firstName;
    private String lastName;
    private List<User> friendList;

    public User(String firstName, String lastName, String userName, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.password = password;
        this.friendList = new ArrayList<>();
    }

    public User() {
        this.firstName = "";
        this.lastName = "";
        this.userName = "";
        this.password = "";
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public List<User> getFriends() {
        return friendList;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public void setFriends(List<User> friendList) {
        this.friendList = friendList;
    }

    public void addFriend(User person) {
        friendList.add(person);
    }

    public void removeFriend(Long id) {
        Predicate<User> isFriend = x -> x.getId().equals(id);
        friendList.removeIf(isFriend);
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return firstName + ' ' + lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return getId().equals(((User) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFirstName(), getLastName(), friendList);
    }
}
