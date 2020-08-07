package org.freemountain.operator;


import io.agroal.api.AgroalDataSource;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@ApplicationScoped
public class MySqlClient {
    @Inject
    AgroalDataSource defaultDataSource;

    public void execute(String sql) throws SQLException {
        try (Connection connection = defaultDataSource.getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute(sql);
        }
    }

    public void execute(List<String> statements) throws SQLException {
        try (Connection connection = defaultDataSource.getConnection()) {
            Statement statement = connection.createStatement();
            for (String sql : statements) {
                statement.execute(sql);
            }
        }
    }

}
