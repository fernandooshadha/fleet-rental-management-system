package lk.ijse.fleet;

import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private List<SystemUser> users = new ArrayList<>();

    public SystemUser registerUser(String username, String password, String fullName) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            System.out.println("Username and password cannot be empty.");
            return null;
        }
        if (findByUsername(username) != null) {
            System.out.println("Username already taken.");
            return null;
        }
        SystemUser user = new SystemUser(username, password, fullName);
        users.add(user);

        System.out.println("User added successfully.");
        return user;
    }

    public SystemUser login(String username, String password) {
        for (SystemUser user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    public SystemUser findByUsername(String username) {
        for (SystemUser user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }
}
