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
    		GameRoom room = new GameRoom(i+1);
    		
    		rooms.add(room);
    	}
    }
}
