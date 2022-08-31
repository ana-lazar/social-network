package controllers;

import domain.UserPage;
import domain.entities.FriendRequest;
import domain.entities.User;
import domain.exceptions.ValidationException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import utils.MainEvent;
import utils.MessageAlert;
import utils.Observer;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SearchController implements Observer<MainEvent> {
    private final ObservableList<User> userModel = FXCollections.observableArrayList();
    private Stage stage;
    private Scene mainScene;
    private UserPage userPage;
    private TextField mainSearchBar;

    @FXML
    ListView<User> userView;
    @FXML
    Label headerLabel;
    @FXML
    TextField searchBar;
    @FXML
    Button sendRequestButton;
    @FXML
    Button unfriendButton;

    public void setPage(UserPage userPage, String initialText) {
        this.userPage = userPage;
        userPage.addControllerObserver(this);
        headerLabel.setText(userPage.getFirstName() + " " + userPage.getLastName() + "'s Profile");
        searchBar.setText(initialText);
        searchBar.requestFocus();
        searchBar.positionCaret(searchBar.getText().length());
        initUsersModel();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setTitle("Social Network");
    }

    public void setScenes(Scene mainScene, TextField mainSearchBar) {
        this.mainScene = mainScene;
        this.mainSearchBar = mainSearchBar;
    }

    @FXML
    public void initialize() {
        sendRequestButton.setDisable(true);
        unfriendButton.setDisable(true);
        userView.setItems(userModel);
        userView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection == null) {
                if (oldSelection != null && userModel.contains(oldSelection)) {
                    userView.getSelectionModel().select(oldSelection);
                }
                else {
                    sendRequestButton.setDisable(true);
                    unfriendButton.setDisable(true);
                }
                return;
            }
            if (userPage.getFriends().contains(newSelection)) {
                sendRequestButton.setDisable(true);
                unfriendButton.setDisable(false);
            }
            else {
                if (userPage.getRequests().stream().filter(request -> request.getFrom().getId().equals(userPage.getId()) && request.getTo().getId().equals(newSelection.getId()) && request.getStatus().equals("PENDING")).count() == 0) {
                    sendRequestButton.setDisable(false);
                    unfriendButton.setDisable(true);
                }
                else {
                    sendRequestButton.setDisable(true);
                    unfriendButton.setDisable(true);
                }
            }
        });
    }

    private void initUsersModel() {
        if (searchBar == null || searchBar.getText().equals("")) {
            userModel.clear();
            return;
        }
        List<User> users = null;
        try {
            users = userPage.getUsers().stream()
                    .filter(entity -> !userPage.getId().equals(entity.getId()) &&
                            (entity.getFirstName().contains(searchBar.getText()) || entity.getLastName().contains(searchBar.getText())))
                    .collect(Collectors.toList());
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }
        userModel.setAll(users);
    }

    @Override
    public void update(MainEvent event) {
        initUsersModel();
    }

    @FXML
    public void handleSearchBar(KeyEvent event) {
        if (searchBar.getText().equals("")) {
            mainSearchBar.clear();
            stage.setScene(mainScene);
        }
        else {
            userView.getSelectionModel().clearSelection();
            initUsersModel();
        }
    }

    @FXML
    public void handleSendRequest(ActionEvent event) {
        if (userView.getSelectionModel().isEmpty()) {
            MessageAlert.showErrorMessage(null, "Please select a user.");
            return;
        }
        try {
            User to = userView.getSelectionModel().getSelectedItem();
            Optional<FriendRequest> optional = userPage.addRequest(to.getId());
            if (optional.isEmpty())
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Friend Request","Friend request to " + to.toString() + " sent!");
        } catch (ValidationException | SQLException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }

    @FXML
    public void handleUnfriend(ActionEvent event) throws SQLException {
        User friend = userView.getSelectionModel().getSelectedItem();
        if (friend == null) {
            MessageAlert.showWarningMessage(null, "Please select an user.");
            return;
        }
        if (userPage.unfriendUser(friend.getId()).isEmpty()) {
            MessageAlert.showWarningMessage(null, "Please select a user.");
        }
        else {
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Removed Friend","Removed " + friend.toString() + " from your friend list.");
        }
    }

    @FXML
    public void handleLogOut(ActionEvent event) {
        stage.close();
    }
}
