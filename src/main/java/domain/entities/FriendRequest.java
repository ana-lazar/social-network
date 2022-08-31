package domain.entities;

import domain.Status;
import domain.Tuple;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FriendRequest extends Entity<Tuple<Long, Long>> {
    private Status status;
    private User from;
    private User to;
    private LocalDateTime date;

    public FriendRequest() {
        status = Status.PENDING;
    }

    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public String getFromName() {
        return from.getFirstName() + " " + from.getLastName();
    }

    public String getToName() {
        return to.getFirstName() + " " + to.getLastName();
    }

    public User getTo() {
        return to;
    }

    public void setTo(User to) {
        this.to = to;
    }

    public void approve() {
        this.status = Status.APPROVED;
    }

    public void reject() {
        this.status = Status.REJECTED;
    }

    public String getStatus() {
        return String.valueOf(status);
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return "From: " + from.getFirstName() + ", To: " + to.getFirstName() + ", Status: " + status + ", Date: " + date.format(formatter);
    }
}
