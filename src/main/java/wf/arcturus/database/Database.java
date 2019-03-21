package wf.arcturus.database;

import com.zaxxer.hikari.*;
import wf.arcturus.*;
import java.sql.*;

public class Database
{
    private HikariDataSource dataSource;
    private DatabasePool databasePool;
    private ConfigurationManager config;
    
    public Database(final ConfigurationManager config) {
        this.config = config;
        final long millis = System.currentTimeMillis();
        boolean SQLException = false;
        try {
            this.databasePool = new DatabasePool();
            if (!this.databasePool.startStoragePooling(config)) {
                System.out.println("Failed to connect to the database. Shutting down...");
                SQLException = true;
                return;
            }
            this.dataSource = this.databasePool.database();
        }
        catch (Exception e) {
            e.printStackTrace();
            SQLException = true;
        }
        finally {
            if (SQLException) {
                System.exit(-1);
            }
        }
        System.out.println("[Database] Connected! (" + (System.currentTimeMillis() - millis) + " MS)");
    }
    
    @Deprecated
    public PreparedStatement prepare(final String query) {
        PreparedStatement statement = null;
        try {
            statement = this.dataSource.getConnection().prepareStatement(query, 1);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return statement;
    }
    
    public void dispose() {
        if (this.databasePool != null) {
            this.databasePool.database().close();
        }
    }
    
    public HikariDataSource dataSource() {
        return this.dataSource;
    }
    
    public DatabasePool databasePool() {
        return this.databasePool;
    }
}
