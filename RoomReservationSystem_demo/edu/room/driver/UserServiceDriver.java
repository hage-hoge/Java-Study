package driver;

import reservation.service.*;

public class UserServiceDriver {
    public static void main(String[] args) {
        UserService userService = new UserService();

        userService.showList();
        
        userService.register();

        
        userService.showList();
    }
}
