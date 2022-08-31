package domain.validators;

import domain.entities.Friendship;
import domain.exceptions.SameIdException;
import domain.exceptions.ValidationException;

public class FriendshipValidator implements Validator<Friendship> {
    @Override
    public void validate(Friendship entity) throws ValidationException {
        if (entity.getId().getLeft().equals(entity.getId().getRight())) {
            throw new SameIdException();
        }
    }
}
