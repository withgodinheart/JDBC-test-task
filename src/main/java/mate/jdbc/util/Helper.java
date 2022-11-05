package mate.jdbc.util;

public class Helper {

    private Helper() {
    }

    /**
     * Forms schema.table string
     *
     * @param schemaKey property name for schema
     * @param tableName name of the db table
     * @return schema.table string
     */
    public static String formDbTableName(String schemaKey, String tableName) {
        String schema = PropertiesLoader.INSTANCE.get(schemaKey);
        return schema == null ? tableName : String.format("%s.%s", schema, tableName);
    }
}
