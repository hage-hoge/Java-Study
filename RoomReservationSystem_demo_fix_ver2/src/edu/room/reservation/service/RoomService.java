package edu.room.reservation.service;

import java.util.Scanner;

import edu.room.reservation.model.*;
import edu.room.reservation.util.TroubleShooting;
import edu.room.reservation.repository.ReservationRepository;
import edu.room.reservation.repository.RoomRepository;

public class RoomService {
    private static final int MAX_ROOM = 30;
    private static final int MAX_LENGTH = 4;
    private RoomRepository roomRepo;
    private ReservationRepository resRepo;
    private User currentUser;
    private Scanner sc = new Scanner(System.in,"MS932");

    public RoomService(RoomRepository roomRepo, ReservationRepository resRepo){
        this.roomRepo = roomRepo;
        this.resRepo = resRepo;
    }

    public void authorize(User currentUser){ this.currentUser = currentUser; }

    public void register(){ 
        while(true){
            try{
                System.out.print("会議室名> "); String roomName = sc.nextLine();
                TroubleShooting.checkString(roomName, MAX_LENGTH);
                TroubleShooting.assertAsciiAlnum(roomName);
                TroubleShooting.isExist(roomRepo, roomName);
                System.out.print("最大収容人数> "); String tmp = sc.nextLine();
                TroubleShooting.assertAsciiAlnum(tmp);
                TroubleShooting.parsePositiveInt(tmp);
                int cap = Integer.parseInt(tmp);
                
                TroubleShooting.checkRepository(roomRepo, MAX_ROOM);

                roomRepo.add(roomName,cap);
                return;
            }catch(IllegalStateException e){
                System.out.println(e.getMessage());
                return;
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
     }

     public void delete(){
        try{
            TroubleShooting.isAdmin(currentUser);
            showList();
            System.out.print("会議室名>"); String targetName = sc.nextLine();
            int targetId = TroubleShooting.isNotExist(roomRepo, targetName );
            Room targetRoom = roomRepo.getMap().get(targetId);
            if(TroubleShooting.confirmDelete()){
                roomRepo.remove(targetId);
                hasReservation(targetRoom);
            }
            //else System.out.println();

            return;
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
     }


     public void showList(){
        try{
            TroubleShooting.checkRepository(roomRepo);
            System.out.println("会議室, 最大収容人数");
            System.out.println("======================================");
            for(Room room : roomRepo.getMap().values()){
                System.out.println(room.getName()+","+room.getCap());
            }
            System.out.println("======================================");
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    private void hasReservation(Room targetRoom){
        for(Reservation r : resRepo.getMap().values()){
            if(r.getRoom().equals(targetRoom)) resRepo.remove(r.getRoom().getId());
        }
    }
}
