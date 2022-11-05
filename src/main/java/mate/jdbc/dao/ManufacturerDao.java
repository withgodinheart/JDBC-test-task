package mate.jdbc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.sql.DataSource;
import mate.jdbc.datasource.DataSourceFactory;
import mate.jdbc.exception.DataProcessingException;
import mate.jdbc.model.Manufacturer;
import mate.jdbc.util.Helper;
import mate.jdbc.util.SqlFunction;

@mate.jdbc.lib.Dao
public class ManufacturerDao implements Dao<Manufacturer> {

    private static final String DB_TABLE_NAME =
            Helper.formDbTableName("datasource.schema", "manufacturers");
    private static final String INSERT_SQL =
            String.format("INSERT INTO %s (name, country) VALUES (?, ?);", DB_TABLE_NAME);
    private static final String SELECT_BY_ID_SQL =
            String.format("SELECT * FROM %s WHERE id = ? AND is_deleted = FALSE;", DB_TABLE_NAME);
    private static final String SELECT_ALL_SQL =
            String.format("SELECT * FROM %s WHERE is_deleted = FALSE;", DB_TABLE_NAME);
    private static final String UPDATE_SQL =
            String.format("UPDATE %s SET name = ?, country = ? WHERE id = ? AND is_deleted = FALSE;", DB_TABLE_NAME);
    private static final String DELETE_SQL =
            String.format("UPDATE %s SET is_deleted = TRUE WHERE id = ?;", DB_TABLE_NAME);

    private final DataSource dataSource;

    public ManufacturerDao() {
        this(DataSourceFactory.initializePostgresDataSource());
    }

    public ManufacturerDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Manufacturer create(Manufacturer entity) {
        Objects.requireNonNull(entity);
        return execute(connection -> create(connection, entity), String.format("Error when creating a manufacturer: %s", entity));
    }

    /**
     * Executes insert prepared statement
     *
     * @param connection connection instance
     * @param entity     entity without id
     * @return entity with id
     * @throws SQLException if error occurred
     */
    private Manufacturer create(Connection connection, Manufacturer entity) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getCountry());
            ps.executeUpdate();
            Long id = fetchGeneratedId(ps);
            entity.setId(id);
            return entity;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Manufacturer> get(Long id) {
        Objects.requireNonNull(id);
        return execute(connection -> findOne(connection, id), String.format("Error when searching for a manufacturer with id: %d", id));
    }

    /**
     * Executes select by id prepared statement
     *
     * @param connection connection instance
     * @param id         id of a row
     * @return Optional of parsed entity or empty Optional if query failed
     * @throws SQLException if error occurred
     */
    private Optional<Manufacturer> findOne(Connection connection, Long id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(parseRow(rs)) : Optional.empty();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Manufacturer> getAll() {
        return execute(this::findAll, "Error when searching for all manufacturers");
    }

    /**
     * Executes select statement
     *
     * @param connection connection instance
     * @return list of parsed entities
     * @throws SQLException if error occurred
     */
    private List<Manufacturer> findAll(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(SELECT_ALL_SQL)) {
            return collectToList(rs);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Manufacturer update(Manufacturer entity) {
        Objects.requireNonNull(entity);
        return execute(connection -> update(connection, entity), String.format("Error when updating a manufacturer: %s", entity));
    }

    /**
     * Executes update prepared statement
     *
     * @param connection connection instance
     * @param entity     data to update
     * @return entity or null if query failed
     * @throws SQLException if error occurred
     */
    private Manufacturer update(Connection connection, Manufacturer entity) throws SQLException {
        Objects.requireNonNull(entity);
        checkIdIsNotNull(entity);
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getCountry());
            ps.setLong(3, entity.getId());
            return executeUpdate(ps) ? entity : null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(Long id) {
        Objects.requireNonNull(id);
        return execute(connection -> delete(connection, id),
                String.format("Error when deleting a manufacturer with id: %d", id));
    }

    /**
     * Executes delete prepared statement
     *
     * @param connection connection instance
     * @param id         id to delete
     * @return true or false
     * @throws SQLException if error occurred
     */
    private boolean delete(Connection connection, Long id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_SQL)) {
            ps.setLong(1, id);
            return executeUpdate(ps);
        }
    }

    /**
     * Executes SqlFunction inside try-catch block
     *
     * @param fnc          SqlFunction
     * @param errorMessage message for exception
     * @param <R>          return type
     * @return execution result
     */
    private <R> R execute(SqlFunction<Connection, ? extends R> fnc, String errorMessage) {
        try (Connection connection = dataSource.getConnection()) {
            return fnc.execute(connection);
        } catch (SQLException e) {
            throw new DataProcessingException(errorMessage, e);
        }
    }

    /**
     * Executes update query and return
     *
     * @param ps prepared statement
     * @return true or false
     * @throws SQLException if error occurred
     */
    private boolean executeUpdate(PreparedStatement ps) throws SQLException {
        int rowsAffected = ps.executeUpdate();
        return rowsAffected > 0;
    }

    /**
     * Fetches generated id from prepared statement
     *
     * @param ps prepared statement
     * @return generated id
     * @throws SQLException if error occurred
     */
    private Long fetchGeneratedId(PreparedStatement ps) throws SQLException {
        ResultSet generatedKeys = ps.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getLong(1);
        } else {
            throw new DataProcessingException("Cannot obtain Id");
        }
    }

    /**
     * Parses result set to entity
     *
     * @param rs result set
     * @return parsed entity
     */
    private Manufacturer parseRow(ResultSet rs) {
        try {
            return createFromResultSet(rs);
        } catch (SQLException e) {
            throw new DataProcessingException("Cannot parse row to create instance", e);
        }
    }

    /**
     * Creates entity from result set
     *
     * @param rs result set
     * @return entity
     * @throws SQLException if error occurred
     */
    private Manufacturer createFromResultSet(ResultSet rs) throws SQLException {
        Manufacturer entity = new Manufacturer();
        entity.setId(rs.getLong("id"));
        entity.setName(rs.getString("name"));
        entity.setCountry(rs.getString("country"));
        return entity;
    }

    /**
     * Collects result set to list of entities
     *
     * @param rs result set
     * @return list of entities
     * @throws SQLException if error occurred
     */
    private List<Manufacturer> collectToList(ResultSet rs) throws SQLException {
        List<Manufacturer> list = new ArrayList<>();
        while (rs.next()) {
            Manufacturer entity = parseRow(rs);
            list.add(entity);
        }
        return list;
    }

    /**
     * Checks if id of entity is not null
     *
     * @param entity entity to check
     */
    private void checkIdIsNotNull(Manufacturer entity) {
        if (entity.getId() == null) {
            throw new DataProcessingException("Id cannot be null");
        }
    }
}
