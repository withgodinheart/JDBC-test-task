package mate.jdbc.datasource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.sql.Connection;
import javax.sql.DataSource;
import mate.jdbc.exception.DataProcessingException;
import mate.jdbc.util.PropertiesLoader;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.postgresql.ds.PGSimpleDataSource;

public class DataSourceFactory {

    private DataSourceFactory() {
    }

    /**
     * Initializes PostgresSQL datasource
     *
     * @return datasource object
     */
    public static DataSource initializePostgresDataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setURL(PropertiesLoader.INSTANCE.get("datasource.url"));
        dataSource.setUser(PropertiesLoader.INSTANCE.get("datasource.user"));
        dataSource.setPassword(PropertiesLoader.INSTANCE.get("datasource.password"));
        dataSource.setCurrentSchema(PropertiesLoader.INSTANCE.get("datasource.schema"));
        initDb(dataSource);
        return dataSource;
    }

    /**
     * Initializes db from resource
     *
     * @param dataSource current datasource
     */
    private static void initDb(DataSource dataSource) {
        String filename = PropertiesLoader.INSTANCE.get("datasource.init.filename");
        if (filename != null) {
            try (Connection connection = dataSource.getConnection()) {
                ScriptRunner runner = new ScriptRunner(connection);
                URL resource = DataSourceFactory.class.getClassLoader().getResource(filename);
                throwDataSourceFactoryExceptionIfNull(resource,
                        "Invalid datasource.init.filename property in application.properties");
                BufferedReader reader = new BufferedReader(new FileReader(resource.getPath()));
                runner.runScript(reader);
            } catch (Exception e) {
                System.err.println("Error when executing init script: " + e.getMessage());
            }
        }
    }

    /**
     * Throws exception if object is null
     *
     * @param object  object to check
     * @param message message for exception
     * @throws DataProcessingException if object is null
     */
    private static void throwDataSourceFactoryExceptionIfNull(Object object, String message) {
        if (object == null) {
            throw new DataProcessingException(message);
        }
    }
}
