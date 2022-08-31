package domain.entities;

import domain.entities.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Message extends Entity<Long> {
    private User from;
    private List<User> to;
    private String text;
    private LocalDateTime date;
    private Message reply = null;

    public Message(User from, String text) {
        this.from = from;
        this.text = text;
    }

    public Message() {

    }

    public User getFrom() {
        return from;
    }

    public String getFromName() {
        return from.getFirstName();
    }

    public List<User> getTo() {
        return to;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setTo(List<User> to) {
        this.to = to;
    }

    public void setReply(Message reply) {
        this.reply = reply;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String str = "Message From: " + from.getFirstName() + " " + from.getLastName() + ", Text: " + text + " (" + date.format(formatter) + ")";
        if (reply != null) {
            str += " reply to " + reply.getId();
        }
        return str;
    }

    public Message getReply() {
        return reply;
    }
}
