package domain.entities;

import domain.Participant;
import utils.MainEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Event extends Entity<Long> implements MainEvent {
    private String title;
    private String description;
    private LocalDate date;
    private List<Participant> participants = new ArrayList<>();

    public Event() {}

    public Event(String title, String description, LocalDate date) {
        this.title = title;
        this.description = description;
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public void addParticipant(Participant participant) {
        participants.add(participant);
    }

    public void removeParticipant(Participant participant) {
        participants.remove(participant);
    }

    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return title + ": " + description + " on " + date.format(formatter);
    }

    public void dismissFrom(User user) {
        for (Participant participant : participants) {
            if (participant.getUser().getId().equals(user.getId())) {
                participant.setNotifications(false);
            }
        }
    }
}
