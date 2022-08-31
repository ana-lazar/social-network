package domain;

import domain.entities.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Friend {
    private final Long idUser;
    private User friend;
    private LocalDateTime date;

    public Friend(Long idUser) {
        this.idUser = idUser;
    }

    public void setFriend(User friend) {
        this.friend = friend;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Long getIdUser() {
        return idUser;
    }

    public User getFriend() {
        return friend;
    }

    public LocalDateTime getDate() {
        return date;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return "Friend: " + friend.getFirstName() + ' ' + friend.getLastName() + " (" + date.format(formatter) + ")";
    }
}
