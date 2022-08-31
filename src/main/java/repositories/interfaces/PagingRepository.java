package repositories.interfaces;

import domain.entities.Entity;
import repositories.paging.Page;
import repositories.paging.Pageable;

import java.sql.SQLException;

public interface PagingRepository<ID, E extends Entity<ID>> extends Repository<ID, E> {
    Page<E> findAll(Pageable pageable) throws SQLException;
}
