package repositories.paging;

import domain.entities.Message;
import domain.validators.Validator;
import repositories.database.MessageDbRepository;
import repositories.interfaces.PagingRepository;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MessagePagedRepository extends MessageDbRepository implements PagingRepository<Long, Message> {
    public MessagePagedRepository(String tableName, Validator<Message> validator) throws SQLException {
        super(tableName, validator);
    }

    @Override
    public Page<Message> findAll(Pageable pageable) throws SQLException {
        Paginator<Message> paginator = new Paginator<>(pageable, findAll());
        return paginator.paginate();
    }

    public Page<Message> findPagedChat(Long from, Long to, Pageable pageable) throws SQLException {
        List<Message> chat = getChat(from, to).stream().sorted(Comparator.comparing(Message::getDate)).collect(Collectors.toList());
        Collections.reverse(chat);
        Paginator<Message> paginator = new Paginator<>(pageable, chat);
        return paginator.paginate();
    }

    @Override
    public Long findId() throws SQLException {
        Long id = 1L;
        while (findOne(id).isPresent()) {
            id++;
        }
        return id;
    }
}
