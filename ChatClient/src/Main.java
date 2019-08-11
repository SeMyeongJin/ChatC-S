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
import javafx.scene.control.*;
import javafx.scene.layout.*;


public class Main extends Application {
	
	Socket socket;
	TextArea textArea;
	Scene loginScene, chattingScene, signupScene;
	
	
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
		window.setTitle("Welcome RanChat");
		
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setVgap(10);
		grid.setHgap(10);
		grid.setPadding(new Insets(5));
		
		GridPane btnGrid = new GridPane();
		btnGrid.setAlignment(Pos.CENTER_RIGHT);
		btnGrid.setHgap(20);
		btnGrid.setPadding(new Insets(1));
		
		Label lbUserID = new Label("User ID");
		grid.add(lbUserID, 0, 1);
		
		TextField txtUserID = new TextField();
		txtUserID.setPromptText("Set your ID");
		grid.add(txtUserID, 1, 1);
		
		Label lbUserPW = new Label("User PW");
		grid.add(lbUserPW, 0, 2);
		
		PasswordField txtUserPW = new PasswordField();
		txtUserPW.setPromptText("Set your password");
		grid.add(txtUserPW, 1, 2);
		
		Button loginBtn = new Button("Sign in");
		Button signupBtn = new Button("Sign up");
		btnGrid.add(loginBtn, 1, 0);
		btnGrid.add(signupBtn, 0, 0);
		
		grid.add(btnGrid, 1, 3);
		
		loginBtn.setOnAction(event -> {
			window.setScene(chattingScene);
		});
		
		signupBtn.setOnAction(event -> {
			window.setScene(signupScene);
		});
		
		loginScene = new Scene(grid, 480, 480);
		window.setScene(loginScene);
	}
	
	public void setSignupScene()
	{
		window = new Stage();
		window.setTitle("Welcome RanChat");
		
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setVgap(10);
		grid.setHgap(10);
		grid.setPadding(new Insets(5));
		
		GridPane btnGrid = new GridPane();
		btnGrid.setAlignment(Pos.CENTER_RIGHT);
		btnGrid.setPadding(new Insets(0));
		
		Label lbUserID = new Label("User ID");
		grid.add(lbUserID, 0, 1);
		
		TextField txtUserID = new TextField();
		txtUserID.setPromptText("Set your ID");
		grid.add(txtUserID, 1, 1);
		
		Label lbUserPW = new Label("User PW");
		grid.add(lbUserPW, 0, 2);
		
		PasswordField txtUserPW = new PasswordField();
		txtUserPW.setPromptText("Set your password");
		grid.add(txtUserPW, 1, 2);
		
		Label lbUserName = new Label("User Name");
		grid.add(lbUserName, 0, 3);
		
		TextField txtUserName = new TextField();
		txtUserName.setPromptText("Set your Name");
		grid.add(txtUserName, 1, 3);
		
		Button signupBtn = new Button("Sign up");
		btnGrid.add(signupBtn, 0, 0);
		
		signupBtn.setOnAction(event -> {
			window.setScene(loginScene);
		});
		
		grid.add(btnGrid, 1, 4);
		
		signupScene = new Scene(grid, 480, 480);
		window.setScene(signupScene);
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
				
		input.setOnAction(event -> {
			send(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");
			input.requestFocus();
		});
				
		Button sendButton = new Button("Send");
				
		sendButton.setOnAction(event->{
			send(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");
			input.requestFocus();
		});
				
		BorderPane pane = new BorderPane();
		pane.setCenter(input);
		pane.setRight(sendButton);
				
		root.setBottom(pane);
		chattingScene = new Scene(root, 760, 480);
		window.setTitle("Chat Client");
		window.setOnCloseRequest(event -> endClient());
	}
	
	@Override
	public void start(Stage primaryStage) {
		beginClient(new String("127.0.0.1"), 9000);
		
		// Draw Scene
		setChattingScene();
		setSignupScene();
		setLoginScene();
		
		window.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	Stage window;
}
