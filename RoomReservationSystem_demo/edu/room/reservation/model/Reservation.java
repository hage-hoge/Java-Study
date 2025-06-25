package reservation.model;

public class Reservation {
    private final int reserveId;
    private final String date, startTime, endTime;
    private final Room reservedRoom;
    private final User owner;

    public Reservation(int reserveId, 
         String date, 
         String startTIme, 
         String endTime, 
         Room reservedRoom,
         User owner){
            this.reserveId = reserveId;
            this.date = date;
            this.startTime = startTIme;
            this.endTime = endTime;
            this.reservedRoom = reservedRoom;
            this.owner = owner;
         }

    public int getId(){ return reserveId; }
    public String getDate(){ return date; }
    public String getStartTime(){ return startTime; }
    public String getEndTime(){ return endTime; }
    public Room getRoom(){ return reservedRoom; }
    public User getOwner() { return owner;  }
}
