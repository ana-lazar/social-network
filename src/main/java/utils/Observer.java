package utils;

import java.sql.SQLException;

public interface Observer<E extends MainEvent> {
    void update(E event) throws SQLException;
}
