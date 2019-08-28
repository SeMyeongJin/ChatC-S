/*
 * 클라이언트는 스레드를 여러개 돌릴 필요가 없기 때문에 스레드풀 노필요 
 * 서버로 메시지를 전송하는 스레드 한개, 메시지를 전달받는 스레드 한개가 필요
 */


package application;

	
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteOrder;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;

import MsgPacker.MessagePacker;
import MsgPacker.MessageProtocol;

public class Main extends Application {
	
	Socket socket;
	TextArea textArea;
	Stage window;
	
	Scene logScene, chatScene, signupScene, roomScene;
	
	MessagePacker msg;
	MessagePacker reMsg;
	
	public void startClient(String IP, int port) 
	{
		Thread thread = new Thread() 
		{
			public void run() 
			{
				try 
				{
					socket = new Socket(IP,port);   
					recv();
				} 
				catch(Exception e) 
				{
					if(!socket.isClosed()) 
					{
						stopClient();
						System.out.println("[서버 접속 실패]");
						Platform.exit();   
					}
				}
			}
		};
		thread.start();  
	}
	
	public void stopClient() 
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
	
    public void recv() 
    {
		while(true) 
		{
			try 
			{
				InputStream in = socket.getInputStream();
				byte[] buffer = new byte[1024];
				int length = in.read(buffer);
				if (length == -1) throw new IOException();   //length가 -1이면 IOException발생
				
				reMsg = new MessagePacker(buffer);
				reMsg.SetEndianType(ByteOrder.BIG_ENDIAN);
				byte protocol = reMsg.getProtocol();
				
				switch(protocol)
				{
				case MessageProtocol.LOGIN_SUCC:
					Platform.runLater(()->{
						window.setScene(roomScene);   
						});
					break;
					
				case MessageProtocol.ROOMENTER:
					Platform.runLater(()->{
						window.setScene(chatScene);   
						});
					break;
				case MessageProtocol.CHAT:
					String message = new String(reMsg.getString());
					Platform.runLater(()->{
						textArea.appendText(message); 
						});
					break;
				}
			} 
			catch(Exception e) 
			{
				stopClient();
				break;
			}
		}
	}
    
    public void send(byte[] message) 
    {
		Thread thread = new Thread() 
		{
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
					stopClient();
				}
			}
		};
		thread.start();
	}
    
    //로그인 씬
    public void setLoginScene() 
    {    	
    	window = new Stage();
    	GridPane logGrid = new GridPane();
    	logGrid.setAlignment(Pos.CENTER);
    	logGrid.setVgap(10);
    	logGrid.setHgap(10);
    	logGrid.setPadding(new Insets(10));
    	
    	Text topTxt = new Text("Login");
    	topTxt.setFont(Font.font("나눔 고딕",FontWeight.LIGHT,30));
    	logGrid.add(topTxt, 0, 0);   	
        
    	// id
    	Label idLb = new Label("ID");
    	TextField idField = new TextField();
    	idField.setPromptText("ID");
      	logGrid.add(idLb, 0, 3);
    	logGrid.add(idField, 0, 4);
    	
    	//pw
    	Label pwLb = new Label("Password");
    	PasswordField pwField = new PasswordField();
    	pwField.setPromptText("Password");
      	logGrid.add(pwLb, 0, 5);
    	logGrid.add(pwField, 0, 6);
    	
    	// buttons
    	BorderPane btnPane = new BorderPane();
   
    	Button logBtn = new Button("Login");
      	Button signBtn = new Button("Sign Up");
      	btnPane.setLeft(logBtn);
      	btnPane.setCenter(signBtn);      	
      	
    	logGrid.add(btnPane, 0, 8);;
    	
    	logBtn.setOnAction(event->{
    		msg = new MessagePacker();
    		msg.SetEndianType(ByteOrder.BIG_ENDIAN);
    		msg.SetProtocol(MessageProtocol.LOGIN);
    		byte[] data = msg.Finish();    	
    		send(data);
    		});
    	
    	signBtn.setOnAction(event->{
    		window.setScene(signupScene);
    	});
    	
        logScene = new Scene(logGrid, 760, 480);
        window.setScene(logScene);
    }
    
    
    public void setSignUpScene() 
    {
    	window = new Stage();
    	GridPane suGrid = new GridPane();
    	suGrid.setAlignment(Pos.CENTER);
    	suGrid.setVgap(10);
    	suGrid.setHgap(10);
    	suGrid.setPadding(new Insets(10));
    	
    	Text topTxt = new Text("Sign Up");
    	topTxt.setFont(Font.font("나눔 고딕",FontWeight.LIGHT,30));
    	suGrid.add(topTxt, 0, 0);   	
        
    	//id
    	Label idLb = new Label("ID 생성");
    	TextField idField = new TextField();
    	idField.setPromptText("ID");
    	suGrid.add(idLb, 0, 3);
    	suGrid.add(idField, 0, 4);
    	
    	//pw
    	Label pwLb = new Label("Password 생성");
    	TextField pwField = new TextField();
    	pwField.setPromptText("Password");
    	suGrid.add(pwLb, 0, 5);
    	suGrid.add(pwField, 0, 6);
    	
    	// buttons
    	BorderPane btnPane = new BorderPane();
    	   
    	Button backBtn = new Button("Back");
      	Button signBtn = new Button("Sign Up");
      	btnPane.setCenter(signBtn);
      	btnPane.setRight(backBtn);      	
      	
      	suGrid.add(btnPane, 0, 8);
    
    	backBtn.setOnAction(event->{
    		window.setScene(logScene);
    	});
    	
        signupScene = new Scene(suGrid, 760, 480);
        window.setScene(signupScene);
    }
    
    public void setLobbyScene() 
    {
    	window = new Stage();
    	GridPane roomGrid = new GridPane();
    	roomGrid.setAlignment(Pos.TOP_CENTER);
    	roomGrid.setVgap(10);
    	roomGrid.setHgap(10);
    	roomGrid.setPadding(new Insets(10));
    	
    	Text topTxt = new Text("Rooms");
    	topTxt.setFont(Font.font("나눔 고딕",FontWeight.LIGHT,30));
    	roomGrid.add(topTxt, 0, 0);   	
    	
    	//room1
    	Text r1Txt = new Text("Room1");
    	Button r1btn = new Button("입장");
    	roomGrid.add(r1Txt, 0, 3); 
    	roomGrid.add(r1btn, 1, 3); 
    	r1btn.setOnAction(event->{
    		//입장버튼 누르면 서버한테 입장패킷 보내고
    		msg = new MessagePacker();
    		msg.SetEndianType(ByteOrder.BIG_ENDIAN);
    		msg.SetProtocol(MessageProtocol.ROOM1);
    		byte[] data = msg.Finish();
    		send(data);
    		//서버에게 룸 리스트 추가 패킷을 받으면 chat씬 입장 
    	});
    	
    	//room2
    	Text r2Txt = new Text("Room2");
    	Button r2btn = new Button("입장");
      	roomGrid.add(r2Txt, 0, 4); 
    	roomGrid.add(r2btn, 1, 4); 
    	r2btn.setOnAction(event->{
    		//입장버튼 누르면 서버한테 입장패킷 보내고
    		msg = new MessagePacker();
    		msg.SetEndianType(ByteOrder.BIG_ENDIAN);
    		msg.SetProtocol(MessageProtocol.ROOM2);
    		byte[] data = msg.Finish();
    		send(data);    
    		});
    	
    	//room3
    	Text r3Txt = new Text("Room3");
    	Button r3btn = new Button("입장");
      	roomGrid.add(r3Txt, 0, 5); 
    	roomGrid.add(r3btn, 1, 5); 
    	r3btn.setOnAction(event->{
    		//입장버튼 누르면 서버한테 입장패킷 보내고
    		msg = new MessagePacker();
    		msg.SetEndianType(ByteOrder.BIG_ENDIAN);
    		msg.SetProtocol(MessageProtocol.ROOM3);
    		byte[] data = msg.Finish();
    		send(data);    	
    		});
    	
    	roomScene = new Scene(roomGrid, 760, 480);
        window.setScene(roomScene);
    }
    
    // 채팅씬
    public void setChatScene() 
    {
    	window = new Stage();
    	
    	BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		HBox hbox = new HBox();    //Border위에 하나의 레이아웃 더 줌
		hbox.setSpacing(5);
				
		//UserName text
		TextField userName = new TextField();
		Text nick = new Text("NickName : ");
		userName.setPrefWidth(150);
		userName.setPromptText("닉네임을 입력하세요.");
		HBox.setHgrow(userName, Priority.ALWAYS);
		
		hbox.getChildren().addAll(nick,userName);//,IPText,portText);  //textBox에 실질적으로 textField추가
		root.setTop(hbox);   //hbox를 젤 위에둠
		
		textArea = new TextArea();  //초기화 시켜줘야한대 
		textArea.setEditable(false);   //수정 불가
		root.setCenter(textArea);     //센터에는 TextArea (top에는 hbox)
		
		//입력창
		TextField input = new TextField();
		input.setPrefWidth(Double.MAX_VALUE);
		
		
		//엔터키
		input.setOnAction(event->{
			msg = new MessagePacker();
			msg.SetEndianType(ByteOrder.BIG_ENDIAN);
			String btnString = new String(userName.getText() + ": " + input.getText() + "\n");
			msg.SetProtocol(MessageProtocol.CHAT);
			msg.add(btnString);
			byte[] data = msg.Finish();
    		send(data);
			input.setText("");  //전송칸 비워주고
			input.requestFocus();  //다시 메세지 보낼 수 있게 focus여기따 맞춤
		});
		
		//보내기 버튼
		Button sendButton = new Button("보내기");
		
		sendButton.setOnAction(event->{
			msg = new MessagePacker();
			msg.SetEndianType(ByteOrder.BIG_ENDIAN);
			String btnString = new String(userName.getText() + ": " + input.getText() + "\n");
			msg.SetProtocol(MessageProtocol.CHAT);
			msg.add(btnString);
			byte[] data = msg.Finish();
    		send(data);
			input.setText("");  //전송칸 비워주고
			input.requestFocus();  //다시 메세지 보낼 수 있게 focus여기따 맞춤
		});
		
		
		BorderPane pane = new BorderPane();
		pane.setCenter(input);
		pane.setRight(sendButton);
		
		root.setBottom(pane);
		chatScene = new Scene(root,400,400);
		window.setTitle("[ 채팅 클라이언트 ]");
		window.setScene(chatScene);
		window.setOnCloseRequest(event-> stopClient());    //닫으면 실행
		
		input.requestFocus();
    } 
    
    //실제로 프로그램을 동작시키는 메소드
	@Override
	public void start(Stage primaryStage) {
		startClient(new String("127.0.0.1"),9000);  //시작하자마자 서버 연결
		
		// Draw Scene
	    setChatScene();
	    setSignUpScene();
	    setLobbyScene();
	    setLoginScene();
	           
	    window.show();
	}
	
	//프로그램의 진입점
	public static void main(String[] args) {
		launch(args);
	}
}
