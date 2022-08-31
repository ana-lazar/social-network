package controllers;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import domain.UserPage;
import domain.entities.Message;
import domain.entities.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import repositories.paging.Page;
import utils.MainEvent;
import utils.MessageAlert;
import utils.Observer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MessengerController implements Observer<MainEvent> {
    private final ObservableList<User> userModel = FXCollections.observableArrayList();
    private VBox chatBox;
    private Rectangle selectedRectangle;
    private Stage stage;
    private Scene mainScene;
    private UserPage userPage;
    private int pageNumber;
    private LocalDateTime currentDate;

    @FXML
    ListView<User> userView;
    @FXML
    Label headerLabel;
    @FXML
    TextField textMessageField;
    @FXML
    ScrollPane chatPane;
    @FXML
    DatePicker fromDatePicker;
    @FXML
    DatePicker toDatePicker;
    @FXML
    Button downloadChatButton;

    public void setPage(UserPage userPage) {
        this.userPage = userPage;
        userPage.addControllerObserver(this);
        headerLabel.setText(userPage.getFirstName() + " " + userPage.getLastName() + "'s Messages");
        initUsersModel();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setTitle("Social Network");
    }

    public void setScenes(Scene mainScene) {
        this.mainScene = mainScene;
    }

    @FXML
    public void initialize() {
        userView.setItems(userModel);
        chatPane.setContent(chatBox);
        downloadChatButton.setDisable(true);
        fromDatePicker.setValue(LocalDate.now());
        toDatePicker.setValue(LocalDate.now());
        userView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                try {
                    pageNumber = -1;
                    chatBox = new VBox(0);
                    currentDate = null;
                    showNextPage();
                    downloadChatButton.setDisable(false);
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
            }
            else {
                downloadChatButton.setDisable(true);
                try {
                    if (oldSelection != null && userPage.getUsers().stream().filter(u -> oldSelection.getId().equals(u.getId())).count() != 0) {
                        userView.getSelectionModel().select(oldSelection);
                    }
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
            }
        });
    }

    private void initUsersModel() {
        List<User> users = userPage.getFriends();
        userModel.setAll(users);
    }

    @Override
    public void update(MainEvent event) {
        initUsersModel();
    }

    private Rectangle getMessageRectangle(Message message) {
        Rectangle rectangle;
        Text text = new Text(message.getText());
        if (message.getFrom().getId().equals(userPage.getId())) {
            rectangle = new Rectangle(text.getLayoutBounds().getWidth() + 20, 20, Paint.valueOf("#758078"));
            rectangle.setLayoutX(330 - text.getLayoutBounds().getWidth());
        } else {
            rectangle = new Rectangle(text.getLayoutBounds().getWidth() + 20, 20, Paint.valueOf("#a2b0a6"));
            rectangle.setLayoutX(10);
        }
        rectangle.setArcWidth(20);
        rectangle.setArcHeight(20);
        rectangle.setLayoutY(0);
        rectangle.setId(message.getId().toString());
        rectangle.setOnMouseClicked(this::handleSelectedRectangle);
        return rectangle;
    }

    private Label getMessageLabel(Message message) {
        Label label = new Label();
        label.setText(message.getText());
        label.setLayoutY(2);
        if (message.getFrom().getId().equals(userPage.getId())) {
            Text text = new Text(message.getText());
            label.setLayoutX(340 - text.getLayoutBounds().getWidth());
        } else {
            label.setLayoutX(20);
        }
        label.setOnMouseClicked(this::handleSelectedLabel);
        label.setStyle("-fx-text-fill: black");
        label.setFont(new Font("Montserrat", 11));
        return label;
    }

    private Label getDateLabel(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        Label label = new Label();
        label.setText(date.format(formatter));
        label.setLayoutY(2);
        label.setLayoutX(150);
        label.setStyle("-fx-text-fill: black");
        label.setFont(new Font("Montserrat", 10));
        return label;
    }

    private AnchorPane getBoxPane() {
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setLayoutX(0);
        anchorPane.setPrefSize(355, 25);
        return anchorPane;
    }

    private void addDatePane() {
        AnchorPane datePane = getBoxPane();
        Label dateLabel = getDateLabel(currentDate);
        datePane.getChildren().add(dateLabel);
        chatBox.getChildren().add(0, datePane);
    }

    private void showNextPage() throws SQLException {
        pageNumber++;
        User friend = userView.getSelectionModel().getSelectedItem();
        Page<Message> messagesPage = userPage.getMessagesOnPage(pageNumber, friend.getId());
        if (messagesPage == null) {
            if (currentDate != null) {
                addDatePane();
            }
            currentDate = null;
            return;
        }
        List<Message> messages = messagesPage.getContent().collect(Collectors.toList());
        AnchorPane anchorPane;
        for (Message message : messages) {
            anchorPane = getBoxPane();
            Rectangle rectangle = getMessageRectangle(message);
            Label messageLabel = getMessageLabel(message);
            anchorPane.getChildren().add(rectangle);
            anchorPane.getChildren().add(messageLabel);
            if (currentDate == null) {
                currentDate = message.getDate();
            } else if (message.getDate().getDayOfYear() != currentDate.getDayOfYear()) {
                addDatePane();
                currentDate = message.getDate();
            }
            chatBox.getChildren().add(0, anchorPane);
        }
        if (chatBox.getChildren().size() < 13) {
            addDatePane();
        }
        for (int i = chatBox.getChildren().size(); i < 13; i++) {
            anchorPane = getBoxPane();
            chatBox.getChildren().add(0, anchorPane);
        }
        if (pageNumber == 0) {
            chatPane.setVvalue(1);
        }
        else {
            chatPane.setVvalue(0.3);
        }
        chatPane.setContent(chatBox);
    }

    @FXML
    public void handleSelectedRectangle(MouseEvent event) {
        Rectangle rectangle = (Rectangle) event.getSource();
        selectRectangle(rectangle);
    }

    @FXML
    public void handleSelectedLabel(MouseEvent event) {
        Label label = (Label) event.getSource();
        Rectangle rectangle = (Rectangle) label.getParent().getChildrenUnmodifiable().get(0);
        selectRectangle(rectangle);
    }

    private void selectRectangle(Rectangle rectangle) {
        if (rectangle.equals(selectedRectangle)) {
            if (rectangle.getLayoutX() == 10) {
                rectangle.setFill(Paint.valueOf("#a2b0a6"));
            }
            else {
                rectangle.setFill(Paint.valueOf("#758078"));
            }
            selectedRectangle = null;
        }
        else {
            if (selectedRectangle != null) {
                if (selectedRectangle.getLayoutX() == 10) {
                    selectedRectangle.setFill(Paint.valueOf("#a2b0a6"));
                }
                else {
                    selectedRectangle.setFill(Paint.valueOf("#758078"));
                }
            }
            rectangle.setFill(Paint.valueOf("#9aba95"));
            selectedRectangle = rectangle;
        }
    }

    @FXML
    public void handleSendButton(ActionEvent event) throws SQLException {
        if (!textMessageField.getText().equals("")) {
            if (selectedRectangle == null) {
                if (!userView.getSelectionModel().isEmpty()) {
                    User friend = userView.getSelectionModel().getSelectedItem();
                    List<Long> selected = new ArrayList<>();
                    selected.add(userView.getSelectionModel().getSelectedItem().getId());
                    userPage.sendMessage(selected, textMessageField.getText());
                    textMessageField.clear();
                    userView.getSelectionModel().select(friend);
                }
                else {
                    MessageAlert.showErrorMessage(null, "Please select a user.");
                }
            }
            else {
                if (!textMessageField.getText().equals("") && !userView.getSelectionModel().isEmpty()) {
                    Message message = userPage.getMessages().stream().filter(message1 -> message1.getId().toString().equals(selectedRectangle.getId())).collect(Collectors.toList()).get(0);
                    if (message.getFrom().getId().equals(userPage.getId())) {
                        MessageAlert.showWarningMessage(null, "You can't reply to your own message.");
                    }
                    else {
                        userPage.sendReply(message.getId(), textMessageField.getText() + " reply");
                    }
                    textMessageField.clear();
                }
                else {
                    MessageAlert.showWarningMessage(null, "Please select a message to reply to.");
                }
            }
        }
    }

    @FXML
    public void handleBackButton() {
        stage.setScene(mainScene);
    }

    @FXML
    public void handleLogOut() {
        stage.close();
    }

    @FXML
    public void handleScroll() throws SQLException {
        if (userView.getSelectionModel().getSelectedItems().isEmpty()) {
            return;
        }
        if (chatPane.getVvalue() == 0) {
            showNextPage();
        }
    }

    @FXML
    public void handleDownloadChat(ActionEvent event) throws FileNotFoundException, DocumentException, SQLException {
        User sender = userView.getSelectionModel().getSelectedItem();
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream("/Users/analazar/Intellij/socialNetwork/data/chat.pdf"));
        document.open();
        document.addTitle("Messages from " + sender.getFirstName() + " " + sender.getLastName() + " to " + userPage.getFirstName() + " " + userPage.getLastName());
        Paragraph text = new Paragraph();
        text.add(new Paragraph("\n"));
        text.add(new Paragraph("Messages from " + sender.getFirstName() + " " + sender.getLastName() + " to " + userPage.getFirstName() + " " + userPage.getLastName()));
        text.add(new Paragraph("\n"));
        LocalDateTime from = fromDatePicker.getValue().atStartOfDay();
        LocalDateTime to = toDatePicker.getValue().atTime(LocalTime.MAX);
        List<Message> messages = userPage.getChatFromPeriod(sender.getId(), from, to);
        for (Message message : messages) {
            text.add(new Paragraph(message.toString()));
        }
        document.add(text);
        document.close();
        MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Chat saved","Chat successfully saved to your data folder.");
    }
}
