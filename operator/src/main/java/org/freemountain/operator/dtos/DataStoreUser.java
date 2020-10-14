package org.freemountain.operator.dtos;

import java.util.Collection;

public class DataStoreUser {
    private final String username;
    private final String password;
    private final Collection<String> roles;

    public DataStoreUser(String username, String password, Collection<String> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Collection<String> getRoles() {
        return roles;
    }

    @Override
    public String toString() {
        return "DataStoreUser{"
                + "username='"
                + username
                + '\''
                + ", password='"
                + password
                + '\''
                + ", roles="
                + roles
                + '}';
    }
}
