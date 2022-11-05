package mate.jdbc.dao;

import java.util.List;
import java.util.Optional;

public interface Dao<T> {

    /**
     * Inserts to db table
     *
     * @param entity data without id to insert
     * @return entity with id
     */
    T create(T entity);

    /**
     * Selects row from db table
     *
     * @param id id of searched row
     * @return Optional of parsed entity
     */
    Optional<T> get(Long id);

    /**
     * Selects all available data from db table
     *
     * @return list of parsed entities
     */
    List<T> getAll();

    /**
     * Updates row in db table
     *
     * @param entity data to update
     * @return entity
     */
    T update(T entity);

    /**
     * Deletes row from db table
     *
     * @param id id of searched row
     * @return true or false
     */
    boolean delete(Long id);
}
