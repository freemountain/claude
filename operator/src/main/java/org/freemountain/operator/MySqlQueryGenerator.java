package org.freemountain.operator;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class MySqlQueryGenerator {

    public String createDatabaseIfNotExists(String dbName) {
        return "CREATE DATABASE IF NOT EXISTS `" + dbName + "`";
    }

    public List<String> createOrAlterUser(String username, String password) {
        List<String> statements = new LinkedList<>();
        statements.add("ALTER USER IF EXISTS '" + username + "'@'%' IDENTIFIED BY '" + password + "';");
        statements.add("CREATE USER IF NOT EXISTS '" + username + "'@'%' IDENTIFIED BY '" + password + "';");

        return statements;
    }

    public String grantPrivileges(String databaseName, String username, Collection<String> roles) {
        return "GRANT " + String.join(", ", roles) + " PRIVILEGES ON `" + databaseName + "`.* TO '" + username + "'@'%';";
    }
}
