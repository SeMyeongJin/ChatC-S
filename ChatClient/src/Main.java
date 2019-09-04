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
	
	String name;
	Text nick = new Text();
	
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
					name = reMsg.getString();
					nick.setText("Name : " + name);
					Platform.runLater(()->{
						window.setScene(roomScene);   
						});
					break;
				case MessageProtocol.SIGNUP_SUCC:
					Platform.runLater(()->{
						window.setScene(logScene);
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
    	logGrid.setAlignment(Pos.TOP_CENTER);
    	logGrid.setVgap(10);
    	logGrid.setHgap(10);
    	logGrid.setPadding(new Insets(10));
    	
    	Text topTxt2 = new Text("Ran Chat");
    	Text topTxt = new Text("Welcome");
    	topTxt.setFont(Font.font("나눔 고딕",FontWeight.LIGHT,30));
    	topTxt2.setFont(Font.font("나눔 고딕",FontWeight.LIGHT,30));
    	topTxt.setId("topTxt");
    	topTxt2.setId("topTxt2");
    	logGrid.add(topTxt, 0, 7);
    	logGrid.add(topTxt2, 1, 7);
        
    	// id
    	Label idLb = new Label("ID");
    	TextField idField = new TextField();
    	idField.setPromptText("ID");
      	logGrid.add(idLb, 0, 18);
    	logGrid.add(idField, 1, 18);
    	
    	//pw
    	Label pwLb = new Label("Password");
    	PasswordField pwField = new PasswordField();
    	pwField.setPromptText("Password");
      	logGrid.add(pwLb, 0, 19);
    	logGrid.add(pwField, 1, 19);
    	
    	// buttons
    	BorderPane btnPane = new BorderPane();
   
    	Button logBtn = new Button("로그인");
      	Button signBtn = new Button("회원가입");
      	btnPane.setCenter(logBtn);
      	btnPane.setLeft(signBtn);      	
      	
    	logGrid.add(btnPane, 1, 20, 2, 1);
    	
    	logBtn.setOnAction(event->{
    		msg = new MessagePacker();
    		msg.SetEndianType(ByteOrder.BIG_ENDIAN);
    		msg.SetProtocol(MessageProtocol.LOGIN);
    		String id = new String(idField.getText());
    		String pw = new String(pwField.getText());
    		msg.add(id);
    		msg.add(pw);
    		byte[] data = msg.Finish();    	
    		send(data);
    		});
    	
    	signBtn.setOnAction(event->{
    		window.setScene(signupScene);
    	});
    	
        logScene = new Scene(logGrid, 450, 540);
        logScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        window.setScene(logScene);
    }
    
    
    public void setSignUpScene() 
    {
    	window = new Stage();
    	GridPane suGrid = new GridPane();
    	suGrid.setAlignment(Pos.TOP_CENTER);
    	suGrid.setVgap(10);
    	suGrid.setHgap(10);
    	suGrid.setPadding(new Insets(10));
    	
    	Text topTxt = new Text("회원가입");
       	topTxt.setId("topTxt");
    	suGrid.add(topTxt, 0, 7);   	
        
    	//id
    	Label idLb = new Label("ID");
    	TextField idField = new TextField();
    	idField.setPromptText("ID");
    	suGrid.add(idLb, 0, 17);
    	suGrid.add(idField, 1, 17);
    	
    	//pw
    	Label pwLb = new Label("PW");
    	TextField pwField = new TextField();
    	pwField.setPromptText("Password");
    	suGrid.add(pwLb, 0, 18);
    	suGrid.add(pwField, 1, 18);
    	
    	//id
    	Label nameLb = new Label("Nickname");
    	TextField nameField = new TextField();
    	nameField.setPromptText("Nickname");
    	suGrid.add(nameLb, 0, 19);
    	suGrid.add(nameField, 1, 19);
    	
    	// buttons
    	BorderPane btnPane = new BorderPane();
    	   
    	Button backBtn = new Button("돌아가기");
      	Button signBtn = new Button("회원가입");
      	btnPane.setCenter(signBtn);
      	btnPane.setLeft(backBtn);      	
      	
      	suGrid.add(btnPane, 1, 20);
      	
      	signBtn.setOnAction(event->{
      		msg = new MessagePacker();
    		msg.SetEndianType(ByteOrder.BIG_ENDIAN);
    		msg.SetProtocol(MessageProtocol.SIGNUP);
    		String id = new String(idField.getText());
    		String pw = new String(pwField.getText());
    		String name = new String(nameField.getText());
    		msg.add(id);
    		msg.add(pw);
    		msg.add(name);
    		byte[] data = msg.Finish();    	
    		send(data);
      	});
      	
    	backBtn.setOnAction(event->{
    		window.setScene(logScene);
    	});
    	
        signupScene = new Scene(suGrid, 450, 540);
        signupScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
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
    	roomGrid.setId("roomGrid");
    	
    	Text topTxt = new Text("채팅방 입장");
    	topTxt.setId("topTxt");
    	roomGrid.add(topTxt, 0, 4, 2, 1);   	
    	
    	//room1
    	Text r1Txt = new Text("1번 채팅방");
    	Button r1btn = new Button("입장하기");
    	roomGrid.add(r1Txt, 0, 12); 
    	roomGrid.add(r1btn, 1, 12); 
    	r1btn.setOnAction(event->{
    		msg = new MessagePacker();
    		msg.SetEndianType(ByteOrder.BIG_ENDIAN);
    		msg.SetProtocol(MessageProtocol.ROOM1);
    		byte[] data = msg.Finish();
    		send(data);
    	});
    	
    	//room2
    	Text r2Txt = new Text("2번 채팅방");
    	Button r2btn = new Button("입장하기");
      	roomGrid.add(r2Txt, 0, 13); 
    	roomGrid.add(r2btn, 1, 13); 
    	r2btn.setOnAction(event->{
    		msg = new MessagePacker();
    		msg.SetEndianType(ByteOrder.BIG_ENDIAN);
    		msg.SetProtocol(MessageProtocol.ROOM2);
    		byte[] data = msg.Finish();
    		send(data);    
    		});
    	
    	//room3
    	Text r3Txt = new Text("3번 채팅방");
    	Button r3btn = new Button("입장하기");
      	roomGrid.add(r3Txt, 0, 14); 
    	roomGrid.add(r3btn, 1, 14); 
    	r3btn.setOnAction(event->{
    		msg = new MessagePacker();
    		msg.SetEndianType(ByteOrder.BIG_ENDIAN);
    		msg.SetProtocol(MessageProtocol.ROOM3);
    		byte[] data = msg.Finish();
    		send(data);    	
    		});
    	
    	//room4
    	Text r4Txt = new Text("4번 채팅방");
    	Button r4btn = new Button("입장하기");
      	roomGrid.add(r4Txt, 0, 15); 
    	roomGrid.add(r4btn, 1, 15); 
    	r4btn.setOnAction(event->{
    		msg = new MessagePacker();
    		msg.SetEndianType(ByteOrder.BIG_ENDIAN);
    		msg.SetProtocol(MessageProtocol.ROOM4);
    		byte[] data = msg.Finish();
    		send(data);    	
    		});
    	
    	//room5
    	Text r5Txt = new Text("5번 채팅방");
    	Button r5btn = new Button("입장하기");
      	roomGrid.add(r5Txt, 0, 16); 
    	roomGrid.add(r5btn, 1, 16); 
    	r5btn.setOnAction(event->{
    		msg = new MessagePacker();
    		msg.SetEndianType(ByteOrder.BIG_ENDIAN);
    		msg.SetProtocol(MessageProtocol.ROOM5);
    		byte[] data = msg.Finish();
    		send(data);    	
    		});
    	
    	//room6
    	Text r6Txt = new Text("6번 채팅방");
    	Button r6btn = new Button("입장하기");
      	roomGrid.add(r6Txt, 0, 17); 
    	roomGrid.add(r6btn, 1, 17); 
    	r6btn.setOnAction(event->{
    		msg = new MessagePacker();
    		msg.SetEndianType(ByteOrder.BIG_ENDIAN);
    		msg.SetProtocol(MessageProtocol.ROOM6);
    		byte[] data = msg.Finish();
    		send(data);    	
    		});
    	
    	//room7
    	Text r7Txt = new Text("7번 채팅방");
    	Button r7btn = new Button("입장하기");
      	roomGrid.add(r7Txt, 0, 18); 
    	roomGrid.add(r7btn, 1, 18); 
    	r7btn.setOnAction(event->{
    		msg = new MessagePacker();
    		msg.SetEndianType(ByteOrder.BIG_ENDIAN);
    		msg.SetProtocol(MessageProtocol.ROOM7);
    		byte[] data = msg.Finish();
    		send(data);    	
    		});
    	
    	//room8
    	Text r8Txt = new Text("8번 채팅방");
    	Button r8btn = new Button("입장하기");
      	roomGrid.add(r8Txt, 0, 19); 
    	roomGrid.add(r8btn, 1, 19); 
    	r8btn.setOnAction(event->{
    		msg = new MessagePacker();
    		msg.SetEndianType(ByteOrder.BIG_ENDIAN);
    		msg.SetProtocol(MessageProtocol.ROOM8);
    		byte[] data = msg.Finish();
    		send(data);    	
    		});
    	
    	roomScene = new Scene(roomGrid, 450, 540);
    	roomScene.getStylesheets().add(getClass().getResource("application2.css").toExternalForm());
        window.setScene(roomScene);
    }
    
    // 채팅씬
    public void setChatScene() 
    {
    	window = new Stage();
    	
    	BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		HBox hBox = new HBox();
		hBox.setSpacing(5);
		
		hBox.getChildren().addAll(nick);
		root.setTop(hBox);
		
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
			String btnString = new String(name + ": " + input.getText() + "\n");
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
			String btnString = new String(name + ": " + input.getText() + "\n");
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
		chatScene = new Scene(root, 450, 540);
		chatScene.getStylesheets().add(getClass().getResource("application2.css").toExternalForm());
		window.setTitle("[채팅 클라이언트 ]");
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
