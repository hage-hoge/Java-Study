package edu.room.reservation.repository;

import java.util.*;

import edu.room.reservation.model.*;

public class UserRepository {
    
    private static final Map<Integer, User> map = new LinkedHashMap<Integer, User>();
    private static int idCounter = 1;
    private int currentId;

    public UserRepository(){
        currentId = signId();
        User initUser = new User(currentId, "admin","admin","ADMIN");
        map.put(currentId, initUser);
    }

    public Map<Integer, User> getMap(){ return map; }

    public void add(String name, String pass, String type){ 
        currentId = signId();
        //User newUser = new User(currentId, name, pass, type);
        map.put(currentId, new User(currentId, name, pass, type));
    }

    public void remove(Integer target){ map.remove(target); }

    private int signId(){ return idCounter++; }

}
