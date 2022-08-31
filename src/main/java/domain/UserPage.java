package domain;

import domain.entities.*;
import repositories.paging.Page;
import services.SocialNetworkService;
import utils.MainEvent;
import utils.Observer;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class UserPage implements Observer<MainEvent> {
    private SocialNetworkService service;
    private final Long id;
    private final String firstName;
    private final String lastName;
    private List<User> friends;
    private List<Message> messages;
    private List<FriendRequest> requests;

    public UserPage(Long id, String firstName, String lastName) throws SQLException {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    private void initLists() throws SQLException {
        friends = service.getUserFriends(id);
        messages = service.getMessagesOf(id);
        requests = service.getRequestsOf(id);
    }

    public void setService(SocialNetworkService service) throws SQLException {
        this.service = service;
        service.addObserver(this);
        initLists();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public List<User> getFriends() {
        return friends;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public List<FriendRequest> getRequests() {
        return requests;
    }

    public Long getId() {
        return id;
    }

    @Override
    public void update(MainEvent event) throws SQLException {
        initLists();
    }

    public void addControllerObserver(Observer<MainEvent> controller) {
        service.addObserver(controller);
    }

    public List<String> getActivity(LocalDateTime from, LocalDateTime to) throws SQLException {
        return service.getActivityFromPeriod(id, from, to);
    }

    public Optional<FriendRequest> addRequest(Long id) throws SQLException {
        return service.addFriendRequest(this.id, id);
    }

    public Optional<FriendRequest> updateRequest(Long id, Status status) throws SQLException {
        return service.setFriendRequestStatus(id, this.id, status);
    }

    public Optional<FriendRequest> cancelRequest(Long id) throws SQLException {
        return service.deleteFriendRequest(this.id, id);
    }

    public Optional<Friendship> unfriendUser(Long id) throws SQLException {
        return service.deleteFriendship(this.id, id);
    }

    public List<User> getUsers() throws SQLException {
        return service.getUsers();
    }

    public List<Message> getChat(Long id) throws SQLException {
        return service.getChat(this.id, id);
    }

    public void sendMessage(List<Long> to, String text) throws SQLException {
        service.sendMessage(id, to, text);
    }

    public void sendReply(Long id, String text) throws SQLException {
        service.sendReply(id, this.id, text);
    }

    public List<Event> getEvents() throws SQLException {
        return service.getEvents();
    }

    public void participate(Event e) throws SQLException {
        service.addParticipant(id, e);
    }

    public void leave(Event e) throws SQLException {
        service.removeParticipant(id, e);
    }

    public void dismissNotifications(Event e) throws SQLException {
        service.dismissNotifications(e, id);
    }

    public List<String> getNotifications() throws SQLException {
        return service.getNotificationsFor(id);
    }

    public void addEvent(String title, String description, LocalDate date) throws SQLException {
        service.addEvent(title, description, date);
    }

    public Page<Message> getMessagesOnPage(int page, Long id) throws SQLException {
        return service.getMessagesOnPage(page, this.id, id);
    }

    public List<Message> getChatFromPeriod(Long id, LocalDateTime from, LocalDateTime to) throws SQLException {
        return service.getChatFromPeriod(id, this.id, from, to);
    }
}
