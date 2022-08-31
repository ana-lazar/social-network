package domain.validators;

import domain.entities.Message;
import domain.entities.User;
import domain.exceptions.EmptyFieldsException;
import domain.exceptions.SameIdException;
import domain.exceptions.ValidationException;

public class MessageValidator implements Validator<Message> {
    @Override
    public void validate(Message entity) throws ValidationException {
        if (entity.getFrom() == null) {
            throw new EmptyFieldsException();
        }
        for (User value : entity.getTo()) {
            if (value == null) {
                throw new EmptyFieldsException();
            }
        }
    }
}
