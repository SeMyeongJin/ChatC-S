package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javafx.application.Platform;

public class Client {

	Socket socket;
	
	public Client(Socket socket)
	{
		this.socket = socket;
		recv();
	}
	
	// 클라이언트로부터 메시지를 전달 받는 메소드
	public void recv()
	{
		Runnable thread = new Runnable()
		{
			public void run()
			{
				try 
				{
					while(true)
					{
						InputStream in = socket.getInputStream();
						byte[] buf = new byte[512];
						int len = in.read(buf);
						while(len == -1) throw new IOException();
						System.out.println(socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName());
						String message = new String(buf, 0, len, "UTF-8");
						for(Client cli : Main.clients) 
						{
							cli.send(message);
						}
					}
				} 
				catch(Exception e)
				{
					try 
					{
						System.out.println(socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName());
					}
					catch (Exception e2)
					{
						e2.printStackTrace();
					}
				}
			}
		};
		Main.thrPool.submit(thread);
	}
	
	// 클라이언트에게 메시지를 전달하는 메소드
	public void send(String message)
	{
		Runnable thread = new Runnable()
		{
			public void run()
			{
				try
				{
					OutputStream out = socket.getOutputStream();
					byte[] buf = message.getBytes("UTF-8");
					out.write(buf);
					out.flush();
				}
				catch(Exception e)
				{
					try 
					{
						System.out.println(socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName());
						Main.clients.remove(Client.this);
						socket.close();
					}
					catch(Exception e2)
					{
						e2.printStackTrace();
					}
				}
			}
		};
		Main.thrPool.submit(thread);
	}
}
