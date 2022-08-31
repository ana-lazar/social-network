package controllers;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import domain.UserPage;
import domain.Status;
import domain.entities.Event;
import domain.entities.FriendRequest;
import domain.entities.User;
import domain.exceptions.ValidationException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import utils.MainEvent;
import utils.MessageAlert;
import utils.Observer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class UserController implements Observer<MainEvent> {
    private final ObservableList<User> userModel = FXCollections.observableArrayList();
    private final ObservableList<FriendRequest> requestsModel = FXCollections.observableArrayList();
    private final ObservableList<String> historyModel = FXCollections.observableArrayList();
    private final ObservableList<Event> eventsModel = FXCollections.observableArrayList();

    private Stage stage;
    private Scene mainScene;
    private UserPage userPage;

    @FXML
    ListView<User> usersView;
    @FXML
    ListView<FriendRequest> requestsView;
    @FXML
    ListView<String> historyView;
    @FXML
    ListView<Event> eventsView;
    @FXML
    ListView<String> notificationsView;
    @FXML
    ComboBox<String> requestsComboBox;
    @FXML
    ComboBox<String> eventsComboBox;
    @FXML
    Label headerLabel;
    @FXML
    Button acceptRequestButton;
    @FXML
    Button denyRequestButton;
    @FXML
    Button cancelRequestButton;
    @FXML
    Button createEventButton;
    @FXML
    Button participateEventButton;
    @FXML
    Button leaveEventButton;
    @FXML
    Button dismissEventButton;
    @FXML
    DatePicker fromDatePicker;
    @FXML
    DatePicker toDatePicker;
    @FXML
    TextField searchBar;
    @FXML
    AnchorPane feedAnchorPane;
    @FXML
    AnchorPane showEventsPane;
    @FXML
    AnchorPane createEventPane;
    @FXML
    TextField eventTitleField;
    @FXML
    TextField eventDescriptionField;
    @FXML
    DatePicker eventDatePicker;

    public void setPage(UserPage userPage) throws SQLException, InterruptedException {
        this.userPage = userPage;
        userPage.addControllerObserver(this);
        initUsersModel();
        headerLabel.setText(userPage.getFirstName() + " " + userPage.getLastName() + "'s Profile");
        requestsComboBox.getSelectionModel().select("Received");
        eventsComboBox.getSelectionModel().select("All");
        showNotifications();
        searchBar.setPromptText("user");
    }

    public void setStage(Stage stage, Scene mainScene) {
        this.stage = stage;
        this.mainScene = mainScene;
        stage.setTitle("Pandy");
    }

    @FXML
    public void initialize() {
        usersView.setItems(userModel);
        requestsView.setItems(requestsModel);
        historyView.setItems(historyModel);
        eventsView.setItems(eventsModel);
        fromDatePicker.setValue(LocalDate.now());
        toDatePicker.setValue(LocalDate.now());
        requestsComboBox.getItems().addAll("Received", "Sent");
        requestsComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            initRequestsModel();
            editRequestsTab();
        });
        eventsComboBox.getItems().addAll("All", "My");
        eventsComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            try {
                initEventsModel();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
            editEventsTab();
        });
        createEventPane.setVisible(false);
        eventDatePicker.setValue(LocalDate.now());
        eventTitleField.setPromptText("title");
        eventDescriptionField.setPromptText("description");
    }

    private void initUsersModel() {
        Iterable<User> users = null;
        users = userPage.getFriends();
        List<User> friendList = StreamSupport.stream(Objects.requireNonNull(users).spliterator(), false).collect(Collectors.toList());
        userModel.setAll(friendList);
    }

    private void initRequestsModel() {
        List<FriendRequest> requests = null;
        Predicate<FriendRequest> predicate;
        if (requestsComboBox.getSelectionModel().getSelectedItem().equals("Received")) {
            predicate = request -> request.getTo().getId().equals(userPage.getId());
        }
        else {
            predicate = request -> request.getFrom().getId().equals(userPage.getId());
        }
        requests = userPage.getRequests().stream()
                .filter(predicate)
                .collect(Collectors.toList());
        requestsModel.setAll(requests);
    }

    private void initHistoryModel() {
        List<String> history = null;
        try {
            LocalDateTime from = fromDatePicker.getValue().atStartOfDay();
            LocalDateTime to = toDatePicker.getValue().atTime(LocalTime.MAX);
            history = userPage.getActivity(from, to);
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }
        historyModel.setAll(history);
    }

    private void initEventsModel() throws SQLException {
        List<Event> events;
        Predicate<Event> predicate;
        if (eventsComboBox.getSelectionModel().getSelectedItem().equals("All")) {
            predicate = event -> event.getDate().isAfter(LocalDate.now());
        }
        else {
            predicate = event -> event.getDate().isAfter(LocalDate.now()) && event.getParticipants().stream().anyMatch(e -> e.getUser().getId().equals(userPage.getId()));
        }
        events = userPage.getEvents().stream()
                .filter(predicate)
                .collect(Collectors.toList());
        eventsModel.setAll(events);
    }

    @Override
    public void update(MainEvent event) throws SQLException {
        initUsersModel();
        initRequestsModel();
        initHistoryModel();
        initEventsModel();
    }

    private void editRequestsTab() {
        if (requestsComboBox.getSelectionModel().getSelectedItem().equals("Received")) {
            acceptRequestButton.setDisable(false);
            denyRequestButton.setDisable(false);
            cancelRequestButton.setDisable(true);
        }
        else {
            acceptRequestButton.setDisable(true);
            denyRequestButton.setDisable(true);
            cancelRequestButton.setDisable(false);
        }
    }

    private void editEventsTab() {
        if (eventsComboBox.getSelectionModel().getSelectedItem().equals("All")) {
            leaveEventButton.setDisable(true);
            participateEventButton.setDisable(false);
            dismissEventButton.setDisable(true);
        }
        else {
            leaveEventButton.setDisable(false);
            participateEventButton.setDisable(true);
            dismissEventButton.setDisable(false);
        }
    }

    @FXML
    public void handleLogOut(ActionEvent event) {
        stage.close();
//        thread.join();
    }

    @FXML
    public void handleAcceptRequest(ActionEvent event) {
        if (requestsView.getSelectionModel().isEmpty()) {
            MessageAlert.showErrorMessage(null, "Please select a request.");
            return;
        }
        try {
            FriendRequest request = requestsView.getSelectionModel().getSelectedItem();
            if (!request.getStatus().equals("PENDING")) {
                MessageAlert.showErrorMessage(null,"You have already responded to this request.");
                return;
            }
            Optional<FriendRequest> optional = userPage.updateRequest(request.getFrom().getId(), Status.APPROVED);
            if (optional.isEmpty()) {
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Friend Request", "Friend request from " + request.getFrom().toString() + " accepted!");
            }
        } catch (ValidationException | SQLException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }

    @FXML
    public void handleDenyRequest(ActionEvent event) {
        if (requestsView.getSelectionModel().isEmpty()) {
            MessageAlert.showErrorMessage(null, "Please select a request.");
            return;
        }
        try {
            FriendRequest request = requestsView.getSelectionModel().getSelectedItem();
            if (!request.getStatus().equals("PENDING")) {
                MessageAlert.showErrorMessage(null,"You have already responded to this request.");
                return;
            }
            Optional<FriendRequest> optional = userPage.updateRequest(request.getFrom().getId(), Status.REJECTED);
            if (optional.isEmpty()) {
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Friend Request","Friend request denied!");
            }
        } catch (ValidationException | SQLException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }

    @FXML
    public void handleCancelRequest(ActionEvent event) {
        if (requestsView.getSelectionModel().isEmpty()) {
            MessageAlert.showErrorMessage(null, "Please select a request.");
            return;
        }
        try {
            FriendRequest request = requestsView.getSelectionModel().getSelectedItem();
            if (request.getStatus().equals("APPROVED")) {
                MessageAlert.showErrorMessage(null,"The user has already accepted this request.");
                return;
            }
            Optional<FriendRequest> optional = userPage.cancelRequest(request.getTo().getId());
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Friend Request", "Friend request cancelled.");
        } catch (ValidationException | SQLException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }

    @FXML
    public void handlePeriodChanged(ActionEvent event) {
        initHistoryModel();
    }

    @FXML
    public void handleDownloadHistory(ActionEvent event) throws FileNotFoundException, DocumentException {
        if (historyModel.isEmpty()) {
            MessageAlert.showErrorMessage(null, "Your account shows no activity in this time period.");
            return;
        }
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream("/Users/analazar/Intellij/socialNetwork/data/activity.pdf"));
        document.open();
        document.addTitle(userPage.getFirstName() + " " + userPage.getLastName() + "'s activity");
        List<String> messages = historyModel.stream().filter(value -> value.startsWith("Message")).collect(Collectors.toList());
        List<String> friends = historyModel.stream().filter(value -> value.startsWith("Friend")).collect(Collectors.toList());
        Paragraph text = new Paragraph();
        text.add(new Paragraph("\n"));
        text.add(new Paragraph(userPage.getFirstName() + " " + userPage.getLastName() + "'s activity"));
        text.add(new Paragraph("\n"));
        text.add(new Paragraph("Messages: \n"));
        for (String line : messages) {
            text.add(new Paragraph(line));
        }
        text.add("\n");
        text.add(new Paragraph("Friends: \n"));
        for (String line : friends) {
            text.add(new Paragraph(line));
        }
        document.add(text);
        document.close();
        MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Activity saved","Activity successfully saved to your data folder.");
    }

    @FXML
    public void handleSearchBar(KeyEvent event) {
        if (searchBar.getText().equals("")) {
            stage.setScene(mainScene);
        }
        else {
            try {
                FXMLLoader searchLoader = new FXMLLoader();
                searchLoader.setLocation(getClass().getResource("/views/searchView.fxml"));
                AnchorPane searchLayout = searchLoader.load();
                Scene searchScene = new Scene(searchLayout);
                SearchController searchController = searchLoader.getController();
                searchController.setPage(userPage, searchBar.getText());
                searchController.setStage(stage);
                searchController.setScenes(mainScene, searchBar);
                stage.setScene(searchScene);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleMessengerButton(ActionEvent event) {
        try {
            FXMLLoader messengerLoader = new FXMLLoader();
            messengerLoader.setLocation(getClass().getResource("/views/messengerView.fxml"));
            AnchorPane searchLayout = messengerLoader.load();
            Scene messengerScene = new Scene(searchLayout);
            MessengerController messengerController = messengerLoader.getController();
            messengerController.setPage(userPage);
            messengerController.setStage(stage);
            messengerController.setScenes(mainScene);
            stage.setScene(messengerScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleCreateEvent(ActionEvent event) {
        showEventsPane.setVisible(false);
        createEventPane.setVisible(true);
    }

    @FXML
    public void handleParticipateEvent(ActionEvent event) {
        if (eventsView.getSelectionModel().isEmpty()) {
            MessageAlert.showErrorMessage(null, "Please select an event.");
            return;
        }
        try {
            Event e = eventsView.getSelectionModel().getSelectedItem();
            if (userPage.getEvents().stream().anyMatch(ev -> ev.getParticipants().stream().anyMatch(p -> p.getUser().getId().equals(userPage.getId()) && p.getEventId().equals(e.getId())))) {
                MessageAlert.showErrorMessage(null,"You are already a participant in this event.");
                return;
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            if (Period.between(LocalDate.now(), e.getDate()).getDays() <= 5) {
                notificationsView.getItems().add(0, LocalDateTime.now().format(formatter) + " " + e.getTitle() + " " + e.getDescription() + " in " + Period.between(LocalDate.now(), e.getDate()).getDays() + " days");
            }
            userPage.participate(e);
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Event", "You have been added to this event.");
        } catch (ValidationException | SQLException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }

    @FXML
    public void handleLeaveEvent(ActionEvent event) {
        if (eventsView.getSelectionModel().isEmpty()) {
            MessageAlert.showErrorMessage(null, "Please select an event.");
            return;
        }
        try {
            Event e = eventsView.getSelectionModel().getSelectedItem();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            userPage.leave(e);
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Event", "You have been removed from this event.");
        } catch (ValidationException | SQLException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }

    @FXML
    public void handleDismissEvent(ActionEvent event) {
        if (eventsView.getSelectionModel().isEmpty()) {
            MessageAlert.showErrorMessage(null, "Please select an event.");
            return;
        }
        try {
            Event e = eventsView.getSelectionModel().getSelectedItem();
            userPage.dismissNotifications(e);
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Event", "Notifications have been turned off.");
        } catch (ValidationException | SQLException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }

    @FXML
    public void handleSaveEvent(ActionEvent event) {
        if (eventTitleField.getText().equals("") || eventDescriptionField.getText().equals("") || eventDatePicker.getValue() == null) {
            MessageAlert.showErrorMessage(null, "Make sure all the fields are filled.");
            return;
        }
        try {
            userPage.addEvent(eventTitleField.getText(), eventDescriptionField.getText(), eventDatePicker.getValue());
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Event", "Event successfully created.");
            eventTitleField.clear();
            eventDescriptionField.clear();
        } catch (ValidationException | SQLException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }

    @FXML
    public void handleBackEvent(ActionEvent event) {
        showEventsPane.setVisible(true);
        createEventPane.setVisible(false);
    }

    public void showNotifications() throws InterruptedException, SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        userPage.getNotifications().forEach(notif -> notificationsView.getItems().add(0, LocalDateTime.now().format(formatter) + " " + notif));
    }
}
