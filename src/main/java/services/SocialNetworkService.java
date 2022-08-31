package services;

import domain.Friend;
import domain.UserPage;
import domain.Status;
import domain.entities.*;
import domain.Tuple;
import domain.exceptions.DuplicatedIdException;
import domain.exceptions.InexistentIdException;
import repositories.interfaces.*;
import repositories.paging.MessagePagedRepository;
import repositories.paging.Page;
import repositories.paging.Pageable;
import repositories.paging.PageableImplementation;
import utils.*;
import utils.Observable;
import utils.Observer;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SocialNetworkService implements Observable<MainEvent> {
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final FriendRequestRepository requestRepository;
    private final MessagePagedRepository messageRepository;
    private final EventRepository eventRepository;

    public SocialNetworkService(UserRepository userRepository, FriendshipRepository friendshipRepository, FriendRequestRepository requestRepository, MessagePagedRepository messageRepository, EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.requestRepository = requestRepository;
        this.messageRepository = messageRepository;
        this.eventRepository = eventRepository;
    }

    public Optional<User> addUser(User user) throws SQLException {
        Optional<User> optional = userRepository.save(user);
        if (optional.isEmpty()) {
            notifyObservers(new DomainEvent<>(DomainEventType.ADD, user));
        }
        return optional;
    }

    public Optional<User> updateUser(User user) throws SQLException {
        Optional<User> optional = userRepository.modify(user);
        if (optional.isEmpty()) {
            notifyObservers(new DomainEvent<>(DomainEventType.UPDATE, user));
        }
        return optional;
    }

    public Optional<User> deleteUser(Long id) throws SQLException {
        if (userRepository.findOne(id).isEmpty()) {
            throw new InexistentIdException();
        }
        friendshipRepository.removeAll(id);
        requestRepository.removeAll(id);
        messageRepository.removeAll(id);
        Optional<User> optional = userRepository.remove(id);
        optional.ifPresent(user -> notifyObservers(new DomainEvent<>(DomainEventType.DELETE, user)));
        return optional;
    }

    public Optional<User> getUser(Long id) throws SQLException {
        return userRepository.findOne(id);
    }

    public Optional<User> getUserByUserName(String userName) throws SQLException {
        return userRepository.findByUserName(userName);
    }

    public List<User> getUsers() throws SQLException {
        return StreamSupport.stream(userRepository.findAll().spliterator(), false).collect(Collectors.toList());
    }

    public List<Friend> getFriendsOf(Long id) throws SQLException {
        if (userRepository.findOne(id).isEmpty()) {
            throw new InexistentIdException();
        }
        return getUsersMap().get(id).getFriends().stream()
                .map(user -> {
                    Friend friend = new Friend(user.getId());
                    friend.setFriend(user);
                    try {
                        friend.setDate(friendshipRepository.findOne(new Tuple<>(id, user.getId())).get().getDate());
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    return friend;
                })
                .collect(Collectors.toList());
    }

    public List<Friend> getFriendsFromMonth(Long id, int month) throws SQLException {
        return getFriendsOf(id).stream()
                .filter(friend -> friend.getDate().getMonthValue() == month)
                .collect(Collectors.toList());
    }

    public List<Friend> getFriendsFromPeriod(Long id, LocalDateTime from, LocalDateTime to) throws SQLException {
        return getFriendsOf(id).stream()
                .filter(friend -> friend.getDate().compareTo(from) >= 0 && friend.getDate().compareTo(to) <= 0)
                .collect(Collectors.toList());
    }

    public List<User> getUserFriends(Long id) throws SQLException {
        if (userRepository.findOne(id).isEmpty()) {
            throw new InexistentIdException();
        }
        return getUsersMap().get(id).getFriends();
    }

    public int getNoCommunities() throws SQLException {
        return userRepository.findCommunities(getUsersMap());
    }

    public List<User> getLargestCommunity() throws SQLException {
        return userRepository.findLargest(getUsersMap());
    }

    private Map<Long, User> getUsersMap() throws SQLException {
        Map<Long, User> users = StreamSupport.stream(userRepository.findAll().spliterator(), false).collect(Collectors.toMap(Entity::getId, user -> user));
        for (Friendship friendship : friendshipRepository.findAll()) {
            User firstUser = users.get(friendship.getId().getLeft());
            User secondUser = users.get(friendship.getId().getRight());
            firstUser.addFriend(secondUser);
            secondUser.addFriend(firstUser);
        }
        return users;
    }

    public Optional<Friendship> addFriendship(Friendship friendship) throws SQLException {
        if (userRepository.findOne(friendship.getId().getLeft()).isEmpty() || userRepository.findOne(friendship.getId().getRight()).isEmpty()) {
            throw new InexistentIdException();
        }
        Optional<Friendship> optional = friendshipRepository.save(friendship);
        if (optional.isEmpty()) {
            notifyObservers(new DomainEvent<>(DomainEventType.ADD, friendship));
        }
        return optional;
    }

    public Optional<Friendship> deleteFriendship(Long firstId, Long secondId) throws SQLException {
        if (userRepository.findOne(firstId).isEmpty() || userRepository.findOne(secondId).isEmpty()) {
            throw new InexistentIdException();
        }
        requestRepository.removeAllFrom(firstId, secondId);
        requestRepository.removeAllFrom(secondId, firstId);
        messageRepository.removeAllFrom(firstId, secondId);
        messageRepository.removeAllFrom(secondId, firstId);
        Optional<Friendship> optional = friendshipRepository.remove(new Tuple<>(firstId, secondId));
        optional.ifPresent(friendship -> notifyObservers(new DomainEvent<>(DomainEventType.DELETE, friendship)));
        return optional;
    }

    public List<Friendship> getFriendships() throws SQLException {
        return StreamSupport.stream(friendshipRepository.findAll().spliterator(), false).collect(Collectors.toList());
    }

    public Map<Tuple<Long, Long>, FriendRequest> getRequestMap() throws SQLException {
        Map<Tuple<Long, Long>, FriendRequest> requests = StreamSupport.stream(requestRepository.findAll().spliterator(), false).collect(Collectors.toMap(Entity::getId, request -> request));
        for (FriendRequest friendRequest : requests.values()) {
            friendRequest.setFrom(userRepository.findOne(friendRequest.getId().getLeft()).get());
            friendRequest.setTo(userRepository.findOne(friendRequest.getId().getRight()).get());
        }
        return requests;
    }

    public Optional<FriendRequest> addFriendRequest(long from, long to) throws SQLException {
        friendshipRepository.findOne(new Tuple<>(from, to)).ifPresent(value -> {
            throw new DuplicatedIdException();
        });
        if (userRepository.findOne(from).isEmpty() || userRepository.findOne(to).isEmpty()) {
            throw new InexistentIdException();
        }
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setDate(LocalDateTime.now());
        friendRequest.setId(new Tuple<>(from, to));
        friendRequest.setFrom(userRepository.findOne(from).get());
        friendRequest.setTo(userRepository.findOne(to).get());
        Optional<FriendRequest> optional = requestRepository.save(friendRequest);
        if (optional.isEmpty()) {
            notifyObservers(new DomainEvent<>(DomainEventType.ADD, friendRequest));
        }
        return optional;
    }

    public Optional<FriendRequest> setFriendRequestStatus(long from, long to, Status status) throws SQLException {
        if (requestRepository.findOne(new Tuple<>(from, to)).isEmpty()) {
            throw new InexistentIdException();
        }
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setId(new Tuple<>(from, to));
        friendRequest.setFrom(userRepository.findOne(from).get());
        friendRequest.setTo(userRepository.findOne(to).get());
        friendRequest.setDate(LocalDateTime.now());
        if (status == Status.APPROVED) {
            friendRequest.approve();
            Friendship friendship = new Friendship(LocalDateTime.now());
            friendship.setId(friendRequest.getId());
            friendshipRepository.save(friendship);
        }
        else {
            friendRequest.reject();
        }
        Optional<FriendRequest> optional = requestRepository.modify(friendRequest);
        notifyObservers(new DomainEvent<>(DomainEventType.UPDATE, friendRequest));
        return optional;
    }

    public Optional<FriendRequest> deleteFriendRequest(Long firstId, Long secondId) throws SQLException {
        if (userRepository.findOne(firstId).isEmpty() || userRepository.findOne(secondId).isEmpty()) {
            throw new InexistentIdException();
        }
        Optional<FriendRequest> optional = requestRepository.remove(new Tuple<>(firstId, secondId));
        optional.ifPresent(request -> notifyObservers(new DomainEvent<>(DomainEventType.DELETE, optional.get())));
        return optional;
    }

    public List<FriendRequest> getFriendRequests() throws SQLException {
        return new ArrayList<>(getRequestMap().values());
    }

    public Optional<Message> sendMessage(long from, List<Long> to, String text) throws SQLException {
        if (userRepository.findOne(from).isEmpty() || to.stream().anyMatch(userId -> {
            try {
                return userRepository.findOne(userId).isEmpty();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return false;
        })) {
            throw new InexistentIdException();
        }
        List<User> all = to.stream()
                .map(idee -> {
                    try {
                        return userRepository.findOne(idee).get();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    return null;
                })
                .collect(Collectors.toList());
        Message message = new Message(userRepository.findOne(from).get(), text);
        message.setTo(all);
        message.setDate(LocalDateTime.now());
        Optional<Message> optional = messageRepository.save(message);
        if (optional.isEmpty()) {
            notifyObservers(new DomainEvent<>(DomainEventType.ADD, message));
        }
        return optional;
    }

    public Optional<Message> sendReply(long replyId, long from, String text) throws SQLException {
        Message message = messageRepository.findOne(replyId).get();
        if (userRepository.findOne(from).isEmpty() || messageRepository.findOne(replyId).isEmpty() || (message.getFrom().getId() != from && message.getTo().stream().noneMatch(user -> user.getId() == from))) {
            throw new InexistentIdException();
        }
        Message newMessage = new Message(userRepository.findOne(from).get(), text);
        List<User> to = new ArrayList<>();
        to.add(message.getFrom());
        newMessage.setTo(to);
        newMessage.setDate(LocalDateTime.now());
        newMessage.setReply(message);
        Optional<Message> optional = messageRepository.save(newMessage);
        if (optional.isEmpty()) {
            notifyObservers(new DomainEvent<>(DomainEventType.ADD, newMessage));
        }
        return optional;
    }

    public List<Message> getChat(Long from, Long to) throws SQLException {
        return StreamSupport.stream(messageRepository.findAll().spliterator(), false)
                .filter(message -> message.getFrom().getId().equals(from) && message.getTo().stream().anyMatch(user -> user.getId().equals(to)) || message.getFrom().getId().equals(to) && message.getTo().stream().anyMatch(user -> user.getId().equals(from)))
                .sorted(Comparator.comparing(Message::getDate))
                .collect(Collectors.toList()
                );
    }

    public List<Message> getChatFromPeriod(Long sender, Long reciever, LocalDateTime from, LocalDateTime to) throws SQLException {
        return getChat(sender, reciever).stream()
                .filter(message -> message.getDate().compareTo(from) >= 0 && message.getDate().compareTo(to) <= 0)
                .filter(message -> message.getFrom().getId().equals(sender))
                .collect(Collectors.toList());
    }

    public List<Message> getMessagesFromPeriod(Long id, LocalDateTime from, LocalDateTime to) throws SQLException {
        return StreamSupport.stream(messageRepository.findAll().spliterator(), false)
                .filter(message -> message.getTo().stream().anyMatch(user -> user.getId().equals(id)))
                .filter(message -> message.getDate().compareTo(from) >= 0 && message.getDate().compareTo(to) <= 0)
                .sorted(Comparator.comparing(Message::getDate))
                .collect(Collectors.toList()
                );
    }

    public List<String> getActivityFromPeriod(Long id, LocalDateTime from, LocalDateTime to) throws SQLException {
        List<Message> messages = getMessagesFromPeriod(id, from, to);
        List<Friend> friends = getFriendsFromPeriod(id, from, to);
        List<Tuple<LocalDateTime, String>> activities = new ArrayList<>();
        for (Message message : messages) {
            activities.add(new Tuple<>(message.getDate(), message.toString()));
        }
        for (Friend friend : friends) {
            activities.add(new Tuple<>(friend.getDate(), friend.toString()));
        }
        activities.sort(Comparator.comparing(Tuple::getLeft));
        return activities.stream()
                .map(Tuple::getRight)
                .collect(Collectors.toList());
    }

    public List<Message> getMessagesOf(Long userId) throws SQLException {
        return StreamSupport.stream(messageRepository.findAll().spliterator(), false)
                .filter(message -> message.getTo().stream().anyMatch(u -> u.getId().equals(userId)) || message.getFrom().getId().equals(userId))
                .sorted(Comparator.comparing(Message::getDate))
                .collect(Collectors.toList()
                );
    }

    public List<FriendRequest> getRequestsOf(Long userId) throws SQLException {
        return getRequestMap().values().stream()
                .filter(request -> request.getFrom().getId().equals(userId) || request.getTo().getId().equals(userId))
                .sorted(Comparator.comparing(FriendRequest::getDate))
                .collect(Collectors.toList()
                );
    }

    public Optional<UserPage> getUserPage(Long userId) throws SQLException {
        Optional<User> optional = getUser(userId);
        if (optional.isEmpty()) {
            return Optional.empty();
        }
        User user = optional.get();
        UserPage userPage = new UserPage(user.getId(), user.getFirstName(), user.getLastName());
        userPage.setService(this);
        return Optional.of(userPage);
    }

    public Optional<Event> addEvent(String title, String description, LocalDate date) throws SQLException {
        Event event = new Event(title, description, date);
        Optional<Event> optional = eventRepository.save(event);
        if (optional.isEmpty()) {
            notifyObservers(new DomainEvent<>(DomainEventType.ADD, event));
        }
        return optional;
    }

    public void addParticipant(Long id, Event e) throws SQLException {
        Optional<User> opUser = userRepository.findOne(id);
        if (opUser.isEmpty()) {
            throw new InexistentIdException();
        }
        Optional<Event> opEvent = eventRepository.findOne(e.getId());
        if (opEvent.isEmpty()) {
            throw new InexistentIdException();
        }
        eventRepository.addParticipant(opUser.get(), e);
        notifyObservers(new DomainEvent<>(DomainEventType.ADD, e));
    }

    public void removeParticipant(Long id, Event e) throws SQLException {
        Optional<User> opUser = userRepository.findOne(id);
        if (opUser.isEmpty()) {
            throw new InexistentIdException();
        }
        Optional<Event> opEvent = eventRepository.findOne(e.getId());
        if (opEvent.isEmpty()) {
            throw new InexistentIdException();
        }
        eventRepository.removeParticipant(opUser.get(), e);
        notifyObservers(new DomainEvent<>(DomainEventType.ADD, e));
    }

    public void dismissNotifications(Event e, Long id) throws SQLException {
        Optional<User> opUser = userRepository.findOne(id);
        if (opUser.isEmpty()) {
            throw new InexistentIdException();
        }
        eventRepository.dismissNotifications(opUser.get(), e);
        notifyObservers(new DomainEvent<>(DomainEventType.DELETE, e));
    }

    public List<String> getNotificationsFor(Long id) throws SQLException {
        return getEvents().stream()
                .filter(event -> event.getDate().isAfter(LocalDate.now()) &&
                        Period.between(LocalDate.now(), event.getDate()).getDays() <= 5 &&
                        event.getParticipants().stream()
                                .anyMatch(e -> e.getUser().getId().equals(id) && e.getNotifications().equals(true)))
                .map(e -> e.getTitle() + " " + e.getDescription() + " in " + Period.between(LocalDate.now(), e.getDate()).getDays() + " days")
                .collect(Collectors.toList());
    }

    public List<Event> getEvents() throws SQLException {
        return StreamSupport.stream(eventRepository.findAll().spliterator(), false).collect(Collectors.toList());
    }

    private List<Observer<MainEvent>> observers = new ArrayList<>();

    @Override
    public void addObserver(Observer<MainEvent> observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer<MainEvent> observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(MainEvent event) {
        observers.stream().forEach(observer -> {
            try {
                observer.update(event);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public Page<Message> getMessagesOnPage(int page, Long from, Long to) throws SQLException {
        Pageable pageable = new PageableImplementation(page, 15);
        return messageRepository.findPagedChat(from, to, pageable);
    }
}
