package repositories.file;

import domain.entities.Friendship;
import domain.Tuple;
import domain.exceptions.DuplicatedIdException;
import domain.exceptions.ValidationException;
import domain.validators.Validator;
import repositories.interfaces.FriendshipRepository;

import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FriendshipFileRepository extends AbstractFileRepository<Tuple<Long, Long>, Friendship> implements FriendshipRepository {
    public FriendshipFileRepository(String fileName, Validator<Friendship> validator) {
        super(fileName, validator);
    }

    @Override
    public Friendship extractEntity(List<String> args) throws ParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        Friendship friendship = new Friendship(LocalDateTime.parse(args.get(0), formatter));
        friendship.setId(new Tuple<>(Long.parseLong(args.get(1)), Long.parseLong(args.get(2))));
        return friendship;
    }

    @Override
    protected String entityToString(Friendship entity) {
        return entity.toString();
    }

    @Override
    public Optional<Friendship> save(Friendship entity) throws ValidationException, IllegalArgumentException, SQLException {
        for (Friendship friendship : entities.values()) {
            if ((entity.getId().getLeft().equals(friendship.getId().getLeft()) && entity.getId().getRight().equals(friendship.getId().getRight())) ||
            (entity.getId().getLeft().equals(friendship.getId().getRight()) && entity.getId().getRight().equals(friendship.getId().getLeft()))) {
                throw new DuplicatedIdException();
            }
        }
        return super.save(entity);
    }

    public void removeAll(Long userId) {
        List<Friendship> friendships = new ArrayList<>(entities.values());
        for (Friendship friendship : friendships) {
            if (friendship.getId().getRight().equals(userId) || friendship.getId().getLeft().equals(userId)) {
                this.remove(friendship.getId());
            }
        }
    }

    @Override
    public Optional<Friendship> findOne(Tuple<Long, Long> id) throws IllegalArgumentException {
        List<Friendship> friendships = new ArrayList<>(entities.values());
        for (Friendship friendship : friendships) {
            if (friendship.getId().equals(id)) {
                return Optional.of(friendship);
            }
        }
        return Optional.empty();
    }
}
