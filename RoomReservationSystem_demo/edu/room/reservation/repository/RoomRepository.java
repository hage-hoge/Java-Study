package reservation.repository;

import java.util.*;
import reservation.model.*;

public class RoomRepository {
    private static final Map<Integer, Room> map = new LinkedHashMap<Integer, Room>();
    private static int idCounter = 1;
    private static int currentId;

    public RoomRepository(){}

    public Map<Integer, Room> getMap(){ return map; }

    public void add(String roomName, int cap){
        currentId = signId();
        map.put(currentId, new Room(currentId, roomName, cap));
    }

    public void remove(Integer targetId){ map.remove(targetId); }

    private int signId(){ return idCounter++; }
}
