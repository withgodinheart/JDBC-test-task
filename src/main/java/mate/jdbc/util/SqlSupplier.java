package mate.jdbc.util;

import java.sql.SQLException;

@FunctionalInterface
public interface SqlSupplier<T> {

    T get() throws SQLException;
}
