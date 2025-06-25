package edu.room.driver;

import edu.room.reservation.repository.UserRepository;
import edu.room.reservation.util.*;

public class UserRepositoryDriver {
    public static void main(String[] args) {
        UserRepository userRepo = new UserRepository();

        try{
            TroubleShooting.checkRepository(userRepo);
            System.out.println("Through!");
        } catch(Exception e){
            System.out.println("catch!" + e);
        }

        try{
            TroubleShooting.isExist(userRepo,"user1");
            System.out.println("Through!");
        } catch(Exception e){
            System.out.println("catch!" + e);
        }
    }
}
