package application;
	
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;


public class Main extends Application {
	
	Socket socket;
	TextArea textArea;
	Scene loginScene, chattingScene;
	
	
	// 클라이언트 실행 메소드
	public void beginClient(String IP, int port)
	{
		Thread thr = new Thread()
		{
			public void run() 
			{
				try
				{
					socket = new Socket(IP, port);
					recv();
				}
				catch (Exception e)
				{
					if (!socket.isClosed())
					{
						endClient();
						System.out.println("End Client");
						Platform.exit();
					}
				}
			}
		};
		thr.start();
	}
	
	// 클라이언트 종료 메소드
	public void endClient()
	{
		try
		{
			if(socket != null && !socket.isClosed())
			{
				socket.close();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	// 서버로부터 메시지를 전달받는 메소드
	public void recv()
	{
		while (true)
		{
			try
			{
				InputStream in = socket.getInputStream();
				byte[] buf = new byte[256];
				int len = in.read(buf);
				if (len == -1) throw new IOException();
				String message = new String(buf, 0, len, "UTF-8");
				Platform.runLater(() ->{
					textArea.appendText(message);
				});
			}
			catch(Exception e)
			{
				endClient();
				break;
			}
		}
	}
	
	// 서버에게 메시지를 전달하는 메소드
	public void send(String message)
	{
		Thread thr = new Thread()
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
					endClient();
				}
			}
	    };
	    thr.start();
	}
	
	public void setLoginScene()
	{
		window = new Stage();
		
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		
		Button loginBtn = new Button("login");
		grid.add(loginBtn, 0, 0);
		
		loginBtn.setOnAction(event -> {
			window.setScene(chattingScene);
		});
		loginScene = new Scene(grid, 760, 480);
		window.setScene(loginScene);
		
	}
	public void setChattingScene()
	{
		window = new Stage();
		
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
				
		HBox hbox = new HBox();
		hbox.setSpacing(100);
				
		TextField userName = new TextField();
		userName.setPrefWidth(150);
		userName.setPromptText("Set Nickname");
		HBox.setHgrow(userName, Priority.ALWAYS);
				
		hbox.getChildren().addAll(userName);
		root.setTop(hbox);
				
		textArea = new TextArea();
		textArea.setEditable(false);
		root.setCenter(textArea);
				
		TextField input = new TextField();
		input.setPrefWidth(Double.MAX_VALUE);
		input.setDisable(true);
				
		input.setOnAction(event -> {
			send(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");
			input.requestFocus();
		});
				
		Button sendButton = new Button("Send");
		sendButton.setDisable(true);
				
		sendButton.setOnAction(event->{
			send(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");
			input.requestFocus();
		});
				
		Button connectionButton = new Button("Connect");
		connectionButton.setOnAction(event -> {
			if (connectionButton.getText().equals("Connect"))
			{
				String IP = new String("127.0.0.1");
				int port = 9000;
				try
				{
					
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				beginClient(IP, port);
				Platform.runLater(() -> {
					textArea.appendText("connect completely\n");
				});
				connectionButton.setText("Disconnect");
				input.setDisable(false);
				sendButton.setDisable(false);
				input.requestFocus();
			}
			else
			{
				endClient();
				Platform.runLater(()-> {
					textArea.appendText("disconnect completely\n");
				});
				connectionButton.setText("Connect");
				input.setDisable(true);
				sendButton.setDisable(true);
			}
		});
				
		BorderPane pane = new BorderPane();
		pane.setLeft(connectionButton);
		pane.setCenter(input);
		pane.setRight(sendButton);
				
		root.setBottom(pane);
		chattingScene = new Scene(root, 760, 480);
		window.setTitle("Chat Client");
		window.setOnCloseRequest(event -> endClient());
				
		connectionButton.requestFocus();
	}
	
	@Override
	public void start(Stage primaryStage) {
		
		// Draw Scene
		setChattingScene();
		setLoginScene();
		
		window.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	Stage window;
}
