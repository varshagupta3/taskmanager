package com.taskmanager.taskmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.jdbc.DataSourceBuilder;
import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null && (databaseUrl.startsWith("postgresql://") || databaseUrl.startsWith("postgres://"))) {
            try {
                // Replace postgres:// with postgresql:// if needed for standard URI parsing
                if (databaseUrl.startsWith("postgres://")) {
                    databaseUrl = databaseUrl.replace("postgres://", "postgresql://");
                }
                
                URI dbUri = new URI(databaseUrl);
                String userInfo = dbUri.getUserInfo();
                String username = userInfo.split(":")[0];
                String password = userInfo.contains(":") ? userInfo.split(":")[1] : "";
                
                String host = dbUri.getHost();
                int port = dbUri.getPort();
                String path = dbUri.getPath();
                
                String dbUrl = "jdbc:postgresql://" + host + (port != -1 ? ":" + port : "") + path;

                return DataSourceBuilder.create()
                        .url(dbUrl)
                        .username(username)
                        .password(password)
                        .driverClassName("org.postgresql.Driver")
                        .build();
            } catch (Exception e) {
                throw new RuntimeException("Failed to configure PostgreSQL DataSource from DATABASE_URL: " + databaseUrl, e);
            }
        }
        
        // Fallback to H2 in-memory database for local/default environment
        return DataSourceBuilder.create()
                .url("jdbc:h2:mem:taskmanager;DB_CLOSE_DELAY=-1")
                .username("sa")
                .password("")
                .driverClassName("org.h2.Driver")
                .build();
    }
}
