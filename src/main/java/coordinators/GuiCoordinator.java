package coordinators;

import controllers.LoginController;
import domain.validators.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import repositories.database.*;
import repositories.interfaces.*;
import repositories.paging.MessagePagedRepository;
import services.SocialNetworkService;

import java.io.IOException;
import java.sql.SQLException;

public class GuiCoordinator extends Application {
    SocialNetworkService service;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        UserRepository userRepository = new UserDbRepository(
                "Users",
                new UserValidator()
        );
        FriendshipRepository friendshipRepository = new FriendshipDbRepository(
                "Friendships",
                new FriendshipValidator()
        );
        FriendRequestRepository requestRepository = new FriendRequestDbRepository(
                "FriendRequests",
                new FriendRequestValidator()
        );
        MessagePagedRepository messageRepository = new MessagePagedRepository(
                "Messages",
                new MessageValidator()
        );
        EventRepository eventRepository = new EventDbRepository(
                "Events",
                new EventValidator()
        );
        service = new SocialNetworkService(userRepository, friendshipRepository, requestRepository, messageRepository, eventRepository);
        initView(primaryStage);
        primaryStage.setTitle("Log In");
        primaryStage.show();
    }

    private void initView(Stage primaryStage) throws IOException, SQLException {
        FXMLLoader loginLoader = new FXMLLoader();
        loginLoader.setLocation(getClass().getResource("/views/loginView.fxml"));
        AnchorPane loginLayout = loginLoader.load();
        Scene loginScene = new Scene(loginLayout);
        primaryStage.setScene(loginScene);
        LoginController controller = loginLoader.getController();
        controller.setSocialNetworkService(service);
    }
}
