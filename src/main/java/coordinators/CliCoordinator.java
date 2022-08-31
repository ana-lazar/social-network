package coordinators;

import domain.entities.Event;
import domain.validators.*;
import repositories.database.*;
import repositories.interfaces.*;
import repositories.paging.MessagePagedRepository;
import services.SocialNetworkService;
import ui.Ui;

import java.sql.SQLException;

public class CliCoordinator {
    public static void main(String[] args) throws SQLException {
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
        Ui ui = new Ui(new SocialNetworkService(userRepository, friendshipRepository, requestRepository, messageRepository, eventRepository));
        ui.run();
    }
}
