package controllers;

import com.google.common.hash.Hashing;
import domain.entities.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.SocialNetworkService;
import utils.MessageAlert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Optional;

public class LoginController {
    SocialNetworkService service;

    @FXML
    TextField usernameField;
    @FXML
    PasswordField passwordField;

    @FXML
    public void initialize() {
        usernameField.setPromptText("username");
        passwordField.setPromptText("password");
    }

    public void setSocialNetworkService(SocialNetworkService service) {
        this.service = service;
    }

    private void showUserWindow(User user) {
        try {
            FXMLLoader userLoader = new FXMLLoader();
            userLoader.setLocation(getClass().getResource("/views/userView.fxml"));
            AnchorPane userLayout = userLoader.load();
            Stage userStage = new Stage();
            userStage.setTitle("User activity");
            userStage.initModality(Modality.WINDOW_MODAL);
            Scene mainScene = new Scene(userLayout);
            userStage.setScene(mainScene);
            UserController userController = userLoader.getController();
            userController.setPage(service.getUserPage(user.getId()).get());
            userController.setStage(userStage, mainScene);
            userStage.show();
        } catch (IOException | SQLException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogIn(ActionEvent event) throws SQLException {
        Optional<User> user = service.getUserByUserName(usernameField.getText());
        if (user.isEmpty()) {
            usernameField.clear();
            passwordField.clear();
            usernameField.setPromptText("username");
            passwordField.setPromptText("password");
            MessageAlert.showErrorMessage(null, "User not found.");
        }
        else {
            String hash = Hashing.sha256()
                    .hashString(passwordField.getText(), StandardCharsets.UTF_8)
                    .toString();
            if (user.get().getPassword().equals(hash)) {
                usernameField.clear();
                passwordField.clear();
                Thread thread = new Thread(() -> {
                    Platform.runLater(() -> showUserWindow(user.get()));
                });
                thread.start();
            }
            else {
                passwordField.clear();
                passwordField.setPromptText("password");
                MessageAlert.showErrorMessage(null, "Incorrect password.");
            }
        }
    }
}
