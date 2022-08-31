package ui;

import domain.Status;
import domain.entities.Friendship;
import domain.Tuple;
import domain.entities.User;
import domain.exceptions.NonIntegerIdException;
import domain.exceptions.ValidationException;
import services.SocialNetworkService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Ui {
    private Scanner scanner;
    private final SocialNetworkService service;

    public Ui(SocialNetworkService service) {
        this.service = service;
    }

    private void addUser(List<String> args) throws SQLException {
        User user = new User(args.get(2), args.get(3), args.get(4), args.get(5));
        long id;
        try {
            id = Long.parseLong(args.get(1));
        }
        catch (NumberFormatException exception) {
            throw new NonIntegerIdException();
        }
        user.setId(id);
        if (service.addUser(user).isEmpty()) {
            System.out.println("User added: " + user.toString());
        }
    }

    private void deleteUser(List<String> args) throws SQLException {
        Long id = Long.parseLong(args.get(1));
        service.deleteUser(id).ifPresent(value -> System.out.println("Deleted user: " + value));
    }

    private void updateUser(List<String> args) throws SQLException {
        User user = new User(args.get(2), args.get(3), args.get(4), args.get(5));
        long id;
        try {
            id = Long.parseLong(args.get(1));
        }
        catch (NumberFormatException exception) {
            throw new NonIntegerIdException();
        }
        user.setId(id);
        if (service.updateUser(user).isEmpty()) {
            System.out.println("Updated user: " + user.toString());
        }
    }

    private void getUser(List<String> args) throws SQLException {
        Long id = Long.parseLong(args.get(1));
        service.getUser(id).ifPresent(value -> System.out.println("Found user: " + value));
    }

    private void getUsers() throws SQLException {
        System.out.println("All users:");
        service.getUsers().forEach(System.out::println);
    }

    private void befriendUsers(List<String> args) throws SQLException {
        long firstId, secondId;
        try {
            firstId = Long.parseLong(args.get(1));
            secondId = Long.parseLong(args.get(2));
        }
        catch (NumberFormatException exception) {
            throw new NonIntegerIdException();
        }
        Friendship friendship = new Friendship(LocalDateTime.now());
        friendship.setId(new Tuple<>(firstId, secondId));
        if (service.addFriendship(friendship).isEmpty()) {
            System.out.println("Friendship added: " + friendship.toString());
        }
    }

    private void unfriendUsers(List<String> args) throws SQLException {
        long firstId, secondId;
        try {
            firstId = Long.parseLong(args.get(1));
            secondId = Long.parseLong(args.get(2));
        }
        catch (NumberFormatException exception) {
            throw new NonIntegerIdException();
        }
        service.deleteFriendship(firstId, secondId).ifPresent(value -> System.out.println("Friendship deleted: " + value.toString()));
    }

    private void getAllFriends() throws SQLException {
        System.out.println("All friendships:");
        service.getFriendships().forEach(System.out::println);
    }

    private void getFriendsOf(List<String> args) throws SQLException {
        long id;
        try {
            id = Long.parseLong(args.get(1));
        }
        catch (NumberFormatException exception) {
            throw new NonIntegerIdException();
        }
        System.out.println("Friends:");
        service.getFriendsOf(id).forEach(System.out::println);
    }

    private void getNoCommunities() throws SQLException {
        System.out.println("Number of communities: " + service.getNoCommunities());
    }

    private void getLargestCommunity() throws SQLException {
        List<User> userList = service.getLargestCommunity();
        if (userList.size() > 0) {
            System.out.println("The largest community (" + userList.size() + ") consists of: ");
            userList.forEach(user -> System.out.println(user + "->"));
        }
    }

    private void filterFriends(List<String> args) throws SQLException {
        long id;
        int month;
        try {
            id = Long.parseLong(args.get(1));
            month = Integer.parseInt(args.get(2));
        }
        catch (NumberFormatException exception) {
            throw new NonIntegerIdException();
        }
        System.out.println("Friends:");
        service.getFriendsFromMonth(id, month).forEach(System.out::println);
    }

    private void sendFriendRequest(List<String> args) throws SQLException {
        long from, to;
        try {
            from = Long.parseLong(args.get(1));
            to = Long.parseLong(args.get(3));
        }
        catch (NumberFormatException exception) {
            throw new NonIntegerIdException();
        }
        if (service.addFriendRequest(from, to).isEmpty()) {
            System.out.println("Added friend request: " + from + " to " + to);
        }
    }

    private void respondFriendRequest(List<String> args) throws SQLException {
        long from, to;
        try {
            to = Long.parseLong(args.get(1));
            from = Long.parseLong(args.get(3));
        }
        catch (NumberFormatException exception) {
            throw new NonIntegerIdException();
        }
        Status status = null;
        if (args.get(4).equals("accept")) {
            status = Status.APPROVED;
        }
        else if (args.get(4).equals("deny")) {
            status = Status.REJECTED;
        }
        else {
            throw new ValidationException("Invalid response");
        }
        if (service.setFriendRequestStatus(from, to, status).isEmpty()) {
            System.out.println("Updated friend request: " + from + " to " + to + " - " + status);
        }
    }

    private void getFriendRequests() throws SQLException {
        System.out.println("All friend requests:");
        service.getFriendRequests().forEach(System.out::println);
    }

    private void sendMessage(List<String> args) throws SQLException {
        long from;
        List<Long> to;
        try {
            from = Long.parseLong(args.get(2));
            to = args.subList(4, args.size()).stream()
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }
        catch (NumberFormatException exception) {
            throw new NonIntegerIdException();
        }
        System.out.println("Text: ");
        String text = scanner.nextLine();
        if (service.sendMessage(from, to, text).isEmpty()) {
            System.out.println("Sent message \"" + text + "\" from " + from + " to" +
                    to.stream().reduce("", (acc, item) -> acc + " " + item, String::concat));
        }
    }

    private void replyToMessage(List<String> args) throws SQLException {
        long from, replyId;
        try {
            replyId = Long.parseLong(args.get(2));
            from = Long.parseLong(args.get(4));
        }
        catch (NumberFormatException exception) {
            throw new NonIntegerIdException();
        }
        System.out.println("Text: ");
        String text = scanner.nextLine();
        if (service.sendReply(replyId, from, text).isEmpty()) {
            System.out.println("Sent reply \"" + text + "\" from " + from);
        }
    }

    private void getMessages(List<String> args) throws SQLException {
        long firstUserId, secondUserId;
        try {
            firstUserId = Long.parseLong(args.get(1));
            secondUserId = Long.parseLong(args.get(2));
        }
        catch (NumberFormatException exception) {
            throw new NonIntegerIdException();
        }
        System.out.println("Mesajele sunt:");
        service.getChat(firstUserId, secondUserId).forEach(System.out::println);
    }

    public void run() {
        System.out.println("Welcome!");
        scanner = new Scanner(System.in);
        String input = "";
        List<String> args;
        while (true) {
            System.out.println("Available commands: add, delete, update, find, users, friends, filter;");
            System.out.println("\t\t\tbefriend, defriend, friendships, largest, communities;");
            System.out.println("\t\t\trequest, respond, requests;");
            System.out.println("\t\t\tmessage, reply, chat.");
            System.out.print("> ");
            input = scanner.nextLine();
            if (input.equals("exit")) {
                break;
            }
            args = Arrays.asList(input.split("\\s"));
            try {
                switch (args.get(0)) {
                    case "add" -> addUser(args);
                    case "delete" -> deleteUser(args);
                    case "update" -> updateUser(args);
                    case "find" -> getUser(args);
                    case "users" -> getUsers();
                    case "befriend" -> befriendUsers(args);
                    case "unfriend" -> unfriendUsers(args);
                    case "friendships" -> getAllFriends();
                    case "friends" -> getFriendsOf(args);
                    case "largest" -> getLargestCommunity();
                    case "communities" -> getNoCommunities();
                    case "filter" -> filterFriends(args);
                    case "request" -> sendFriendRequest(args);
                    case "respond" -> respondFriendRequest(args);
                    case "requests" -> getFriendRequests();
                    case "message" -> sendMessage(args);
                    case "reply" -> replyToMessage(args);
                    case "chat" -> getMessages(args);
                    default -> System.out.println("Invalid command!");
                }
            }
            catch (Exception exception) {
                System.out.println(exception.getMessage());
            }
        }
    }
}
