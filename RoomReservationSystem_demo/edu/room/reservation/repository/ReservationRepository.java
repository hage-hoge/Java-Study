package reservation.repository;

import java.util.*;
import reservation.model.*;

public class ReservationRepository {
    private static final Map<Integer, Reservation> map = new LinkedHashMap<Integer, Reservation>();
    private static int idCounter = 1;
    private int currentId;

    public ReservationRepository(){}

    public Map<Integer, Reservation> getMap(){ return map; }

    public void add(String date, String startTime, String endTime, User owner, Room reservedRoom){
        currentId = signId();
        map.put(currentId, new Reservation(currentId, date, startTime, endTime, reservedRoom, owner));
    }

    public void remove(Integer targetId){ map.remove(targetId); }

    private int signId(){ return idCounter++; }
}   
