package domain.validators;

import domain.entities.Event;
import domain.entities.FriendRequest;
import domain.exceptions.EmptyFieldsException;
import domain.exceptions.SameIdException;
import domain.exceptions.ValidationException;

public class EventValidator implements Validator<Event> {
    @Override
    public void validate(Event entity) throws ValidationException {
        if (entity.getTitle().equals("") || entity.getDescription().equals("")) {
            throw new EmptyFieldsException();
        }
    }
}
