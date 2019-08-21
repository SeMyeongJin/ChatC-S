package application;

import java.util.Vector;

public class GameRoom {
	
	private int roomNum;
	private int curUserNum;
	private Vector<Client> users;
	
	public GameRoom(int num)
	{
		roomNum = num;
		curUserNum = 0;
		users = new Vector<Client>();
	}
		
	public void enterRoom(Client user, int roomNum)
	{
		RoomManager.rooms.get(roomNum-1).curUserNum++;
		RoomManager.rooms.get(roomNum-1).users.add(user);
		user.enterRoom(this);
	}
	
	public void leaveRoom(Client user) 
	{
		user.exitRoom();
		users.remove(user);
		curUserNum--;
	}

	
	public int getRoomNum() 
	{
	    return roomNum;
	}
	
	public int getUserNum()
	{
		return curUserNum;
	}

	public Vector<Client> getUserList() 
	{
	    return users;
	}
}

