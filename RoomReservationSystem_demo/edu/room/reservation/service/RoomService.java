package reservation.service;

import java.util.Scanner;

import reservation.model.*;
import reservation.util.TroubleShooting;
import reservation.repository.RoomRepository;

public class RoomService {
    private static final int MAX_ROOM = 30;
    private static final RoomRepository roomRepo = new RoomRepository();
    private User currentUser;
    private Scanner sc = new Scanner(System.in,"MS932");

    public RoomService(){}

    public void authorize(User currentUser){ this.currentUser = currentUser; }

    public void register(){ 
        boolean success = false;
        while(!success){
            try{
                System.out.print("Room Name> "); String roomName = sc.nextLine();
                TroubleShooting.checkString(roomName, "StringError");
                TroubleShooting.isExist(roomRepo, roomName);
                System.out.print("Room Capacity> "); int cap = sc.nextInt();
                TroubleShooting.checkInt(cap, MAX_ROOM);
                success = true;

                roomRepo.add(roomName,cap);
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
     }

     public void delete(){
        try{
            TroubleShooting.isAdmin(currentUser);
            showList();
            System.out.print("Select delete target >"); int targetId = sc.nextInt();
            TroubleShooting.isExist(roomRepo, targetId);
            if(TroubleShooting.confirmDelete()) roomRepo.remove(targetId);
            else System.out.println("Canceled Delete");

            return;
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
     }

     public void showList(){
        try{
            TroubleShooting.checkRepository(roomRepo);
            for(Room room : roomRepo.getMap().values()){
                System.out.println(room.getName()+","+room.getCap());
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
