package application;
	
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;


public class Main extends Application {
	
	public static ExecutorService thrPool;
	public static Vector<Client> clients = new Vector<Client>();
	
	ServerSocket serverSocket;
	TextArea textArea;
	// 서버 프로그램 실행
	public void beginServer(String IP, int port)
	{
		try
		{
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(IP, port));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			if (!serverSocket.isClosed())
			{
				endServer();
			}
			return;
		}
		
		Runnable thread = new Runnable()
		{
			public void run() 
			{
				while(true)
				{
					try
					{
						Socket socket = serverSocket.accept();
						clients.add(new Client(socket));
						Platform.runLater(() -> {
							String message = String.format("%s is entered.\n", socket.getRemoteSocketAddress().toString());
							textArea.appendText(message);
						});
						System.out.println(socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName());
					}
					catch(Exception e)
					{
						if (!serverSocket.isClosed())
						{
							endServer();
						}
						break;
					}
				}
			}
		};
		thrPool = Executors.newCachedThreadPool();
		thrPool.submit(thread);
	}
	
	// 서버 프로그램 종료
	public void endServer()
	{
		try
		{
			Iterator<Client> iter = clients.iterator();
			while (iter.hasNext())
			{
				Client client = iter.next();
				client.socket.close();
				iter.remove();
			}
			if (serverSocket != null && !serverSocket.isClosed())
			{
				serverSocket.close();
			}
			if (thrPool != null && !thrPool.isShutdown())
			{
				thrPool.shutdown();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	// UI
	@Override
	public void start(Stage primaryStage) 
	{
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("나눔 고딕", 12));
		root.setCenter(textArea);
		
		Button toggleButton = new Button("Start");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1, 0, 0, 0));
		root.setBottom(toggleButton);
		
		String IP = "127.0.0.1";
		int port = 9000;
		
		toggleButton.setOnAction(event -> {
			if (toggleButton.getText().equals("Start"))
			{
				beginServer(IP, port);
				Platform.runLater(() -> {
					String message = String.format("Chat Server Start\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("End");
				});
			}
			else
			{
				endServer();
				Platform.runLater(() -> {
					String message = String.format("Chat Server End\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("Start");
				});
			}
		});
		
		Scene scene = new Scene(root, 400, 550);
		primaryStage.setTitle("Chat Server");
		primaryStage.setOnCloseRequest(event -> endServer());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	public static void main(String[] args) 
	{
		launch(args);
	}
}
