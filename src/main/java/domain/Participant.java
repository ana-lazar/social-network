package domain;

import domain.entities.User;

public class Participant {
    private Long eventId;
    private User user;
    private Boolean notifications;

    public Participant(Long eventId, User user, Boolean notifications) {
        this.eventId = eventId;
        this.user = user;
        this.notifications = notifications;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getNotifications() {
        return notifications;
    }

    public void setNotifications(Boolean notifications) {
        this.notifications = notifications;
    }
}
