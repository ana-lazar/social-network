package domain.validators;

import domain.exceptions.ValidationException;

public interface Validator<T> {
    public void validate(T entity) throws ValidationException;
}
