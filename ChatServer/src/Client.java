/*
 * �Ѹ��� Ŭ���̾�Ʈ�� ����ϱ� ���� ����� ��Ƴ��� Ŭ����
 */

package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteOrder;

import MsgPacker.*;

public class Client {
	
	Socket socket;
	Client cli;
	private GameRoom room;
	int roomNum;
	
	MessagePacker msg;
	
	public Client(Socket sock) 
	{
		this.socket = sock;   
		cli = this;
		receive();
	}
	
	//Ŭ���̾�κ��� �޽����� ���޹޴� �޼ҵ�
	public void receive()//()
	{
		// �ϳ��� ������ ���� �� Runnable��ü ���� �̿�
		Runnable thread = new Runnable() 
		{
			// Runnable �ȿ� run�Լ� �־���� run�Լ��� �ϳ��� �����尡 � ���μ� �����Ұ����� ����
			@Override
			public void run() {
				try {
					while(true) {
						InputStream in = socket.getInputStream();
						byte[] buffer = new byte[1024];
						int length = in.read(buffer);
						while(length == -1) throw new IOException();
						System.out.println("[�޽��� ���� ����]" 
						+ socket.getRemoteSocketAddress()
						+ ": " + Thread.currentThread().getName());

						msg = new MessagePacker(buffer);
						byte protocol = msg.getProtocol();
						String chatStr;
	
						switch(protocol)
						{
						case MessageProtocol.LOGIN:{
							//���� ������
							MessagePacker reMsg = new MessagePacker();
							reMsg.SetEndianType(ByteOrder.BIG_ENDIAN);
							reMsg.SetProtocol(MessageProtocol.LOGIN_SUCC);
							byte[] message = reMsg.Finish();
							send(message);
							break;
						 }
						case MessageProtocol.ROOM1:{
							RoomManager.rooms.get(0).enterRoom(cli,0);
							MessagePacker reMsg = new MessagePacker();
							reMsg.SetEndianType(ByteOrder.BIG_ENDIAN);
							reMsg.SetProtocol(MessageProtocol.ROOMENTER);
							byte[] message = reMsg.Finish();
							send(message);
							break;
						 }
						case MessageProtocol.ROOM2:{
							RoomManager.rooms.get(1).enterRoom(cli,1);
							MessagePacker reMsg = new MessagePacker();
							reMsg.SetEndianType(ByteOrder.BIG_ENDIAN);
							reMsg.SetProtocol(MessageProtocol.ROOMENTER);
							byte[] message = reMsg.Finish();
							send(message);
							break;
						 }
						case MessageProtocol.ROOM3:{
							RoomManager.rooms.get(2).enterRoom(cli,2);
							MessagePacker reMsg = new MessagePacker();
							reMsg.SetEndianType(ByteOrder.BIG_ENDIAN);
							reMsg.SetProtocol(MessageProtocol.ROOMENTER);
							byte[] message = reMsg.Finish();
							send(message);
							break;
						 }
						case MessageProtocol.CHAT:{
							roomNum = room.getRoomNum();
							chatStr = msg.getString();
							MessagePacker reMsg = new MessagePacker();
							reMsg.SetEndianType(ByteOrder.BIG_ENDIAN);
							reMsg.SetProtocol(MessageProtocol.CHAT);
							reMsg.add(chatStr);
							byte[] message = reMsg.Finish();
							for(Client client : RoomManager.rooms.get(roomNum).getUserList())
							{
								client.send(message);
							}
							break;
						 }
						}
					}
				} 
				catch(Exception e) 
				{
					try 
					{
						System.out.println("[�޽��� ���� ����]"
						+ socket.getRemoteSocketAddress()
						+ ": " + Thread.currentThread().getName() + e);
					}
					catch(Exception e2) 
					{
					   e.printStackTrace();
					}
				}
			}
		};
		Main.threadPool.submit(thread);   // ������Ǯ�� ������� ������ ���
	}
	
	//Ŭ���̾�Ʈ���� �޽����� �����ϴ� �޼ҵ�
	public void send(byte[] message)
	{
		Runnable thread = new Runnable() 
		{
			@Override
			public void run() 
			{
				try 
				{
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message;
					out.write(buffer);
					out.flush();
				} 
				catch(Exception e) 
				{
					try 
					{
						System.out.println("[�޽��� �۽� ����]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						Main.clients.remove(Client.this);
						socket.close();
					}
					catch(Exception e2) 
					{
						   e.printStackTrace();
					}
				}
			}
		};
		Main.threadPool.submit(thread);
	}

    
    public void enterRoom(GameRoom room) 
    {
      this.room = room;
    }

    public void exitRoom()
    {
        this.room = null;
    }

    public GameRoom getRoom() 
    {
        return room;
    }

    public void setRoom(GameRoom room) 
    {
        this.room = room;
    }

    public Socket getSock() 
    {
        return socket;
    }

    public void setSock(Socket sock) 
    {
        this.socket = sock;
    }
    
    public int getRoomNum() 
    {
        return roomNum;
    }

    public void setRoomNum(int Num) 
    {
        this.roomNum = Num;
    }
}
