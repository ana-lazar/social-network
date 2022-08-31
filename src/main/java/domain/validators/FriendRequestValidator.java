package domain.validators;

import domain.entities.FriendRequest;
import domain.exceptions.SameIdException;
import domain.exceptions.ValidationException;

public class FriendRequestValidator implements Validator<FriendRequest> {
    @Override
    public void validate(FriendRequest entity) throws ValidationException {
        if (entity.getId().getLeft().equals(entity.getId().getRight())) {
            throw new SameIdException();
        }
    }
}
