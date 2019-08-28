package application;

import java.util.Vector;

public class RoomManager {

    public static Vector<GameRoom> rooms = new Vector<GameRoom>();
    
    public static final int MAXROOMNUM = 100;
    
    public RoomManager() {}
    
    public static void createRoom()
    {
    	for (int i = 0; i < MAXROOMNUM; i++)
    	{
    		GameRoom room = new GameRoom(i);
    		
    		rooms.add(room);
    	}
    	System.out.println("create 100 rooms of game completely\n");
    }
}
