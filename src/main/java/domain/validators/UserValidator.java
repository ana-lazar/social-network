package domain.validators;

import domain.entities.User;
import domain.exceptions.EmptyFieldsException;
import domain.exceptions.ValidationException;

public class UserValidator implements Validator<User> {
    @Override
    public void validate(User entity) throws ValidationException {
        String message = "";
        if (entity.getFirstName().equals("") && entity.getLastName().equals("") || entity.getUserName().equals("") || entity.getPassword().equals("")) {
            throw new EmptyFieldsException();
        }
    }
}
