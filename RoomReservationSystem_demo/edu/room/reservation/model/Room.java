package reservation.model;

public class Room {
    private final int roomId;
    private final String roomName;
    private final int cap;

    public Room(int roomId, String roomName, int cap){
        this.roomId = roomId;
        this.roomName = roomName;
        this.cap = cap;
    }

    public int getId(){ return roomId; }
    public String getName() { return roomName; }
    public int getCap() { return cap; }
}
