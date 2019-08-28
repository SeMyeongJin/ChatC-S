/*
 * Ŭ���̾�Ʈ�� �����带 ������ ���� �ʿ䰡 ���� ������ ������Ǯ ���ʿ� 
 * ������ �޽����� �����ϴ� ������ �Ѱ�, �޽����� ���޹޴� ������ �Ѱ��� �ʿ�
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
						System.out.println("[���� ���� ����]");
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
				if (length == -1) throw new IOException();   //length�� -1�̸� IOException�߻�
				
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
    
    //�α��� ��
    public void setLoginScene() 
    {    	
    	window = new Stage();
    	GridPane logGrid = new GridPane();
    	logGrid.setAlignment(Pos.CENTER);
    	logGrid.setVgap(10);
    	logGrid.setHgap(10);
    	logGrid.setPadding(new Insets(10));
    	
    	Text topTxt = new Text("Login");
    	topTxt.setFont(Font.font("���� ���",FontWeight.LIGHT,30));
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
    	topTxt.setFont(Font.font("���� ���",FontWeight.LIGHT,30));
    	suGrid.add(topTxt, 0, 0);   	
        
    	//id
    	Label idLb = new Label("ID ����");
    	TextField idField = new TextField();
    	idField.setPromptText("ID");
    	suGrid.add(idLb, 0, 3);
    	suGrid.add(idField, 0, 4);
    	
    	//pw
    	Label pwLb = new Label("Password ����");
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
    	topTxt.setFont(Font.font("���� ���",FontWeight.LIGHT,30));
    	roomGrid.add(topTxt, 0, 0);   	
    	
    	//room1
    	Text r1Txt = new Text("Room1");
    	Button r1btn = new Button("����");
    	roomGrid.add(r1Txt, 0, 3); 
    	roomGrid.add(r1btn, 1, 3); 
    	r1btn.setOnAction(event->{
    		//�����ư ������ �������� ������Ŷ ������
    		msg = new MessagePacker();
    		msg.SetEndianType(ByteOrder.BIG_ENDIAN);
    		msg.SetProtocol(MessageProtocol.ROOM1);
    		byte[] data = msg.Finish();
    		send(data);
    		//�������� �� ����Ʈ �߰� ��Ŷ�� ������ chat�� ���� 
    	});
    	
    	//room2
    	Text r2Txt = new Text("Room2");
    	Button r2btn = new Button("����");
      	roomGrid.add(r2Txt, 0, 4); 
    	roomGrid.add(r2btn, 1, 4); 
    	r2btn.setOnAction(event->{
    		//�����ư ������ �������� ������Ŷ ������
    		msg = new MessagePacker();
    		msg.SetEndianType(ByteOrder.BIG_ENDIAN);
    		msg.SetProtocol(MessageProtocol.ROOM2);
    		byte[] data = msg.Finish();
    		send(data);    
    		});
    	
    	//room3
    	Text r3Txt = new Text("Room3");
    	Button r3btn = new Button("����");
      	roomGrid.add(r3Txt, 0, 5); 
    	roomGrid.add(r3btn, 1, 5); 
    	r3btn.setOnAction(event->{
    		//�����ư ������ �������� ������Ŷ ������
    		msg = new MessagePacker();
    		msg.SetEndianType(ByteOrder.BIG_ENDIAN);
    		msg.SetProtocol(MessageProtocol.ROOM3);
    		byte[] data = msg.Finish();
    		send(data);    	
    		});
    	
    	roomScene = new Scene(roomGrid, 760, 480);
        window.setScene(roomScene);
    }
    
    // ä�þ�
    public void setChatScene() 
    {
    	window = new Stage();
    	
    	BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		HBox hbox = new HBox();    //Border���� �ϳ��� ���̾ƿ� �� ��
		hbox.setSpacing(5);
				
		//UserName text
		TextField userName = new TextField();
		Text nick = new Text("NickName : ");
		userName.setPrefWidth(150);
		userName.setPromptText("�г����� �Է��ϼ���.");
		HBox.setHgrow(userName, Priority.ALWAYS);
		
		hbox.getChildren().addAll(nick,userName);//,IPText,portText);  //textBox�� ���������� textField�߰�
		root.setTop(hbox);   //hbox�� �� ������
		
		textArea = new TextArea();  //�ʱ�ȭ ��������Ѵ� 
		textArea.setEditable(false);   //���� �Ұ�
		root.setCenter(textArea);     //���Ϳ��� TextArea (top���� hbox)
		
		//�Է�â
		TextField input = new TextField();
		input.setPrefWidth(Double.MAX_VALUE);
		
		
		//����Ű
		input.setOnAction(event->{
			msg = new MessagePacker();
			msg.SetEndianType(ByteOrder.BIG_ENDIAN);
			String btnString = new String(userName.getText() + ": " + input.getText() + "\n");
			msg.SetProtocol(MessageProtocol.CHAT);
			msg.add(btnString);
			byte[] data = msg.Finish();
    		send(data);
			input.setText("");  //����ĭ ����ְ�
			input.requestFocus();  //�ٽ� �޼��� ���� �� �ְ� focus����� ����
		});
		
		//������ ��ư
		Button sendButton = new Button("������");
		
		sendButton.setOnAction(event->{
			msg = new MessagePacker();
			msg.SetEndianType(ByteOrder.BIG_ENDIAN);
			String btnString = new String(userName.getText() + ": " + input.getText() + "\n");
			msg.SetProtocol(MessageProtocol.CHAT);
			msg.add(btnString);
			byte[] data = msg.Finish();
    		send(data);
			input.setText("");  //����ĭ ����ְ�
			input.requestFocus();  //�ٽ� �޼��� ���� �� �ְ� focus����� ����
		});
		
		
		BorderPane pane = new BorderPane();
		pane.setCenter(input);
		pane.setRight(sendButton);
		
		root.setBottom(pane);
		chatScene = new Scene(root,400,400);
		window.setTitle("[ ä�� Ŭ���̾�Ʈ ]");
		window.setScene(chatScene);
		window.setOnCloseRequest(event-> stopClient());    //������ ����
		
		input.requestFocus();
    } 
    
    //������ ���α׷��� ���۽�Ű�� �޼ҵ�
	@Override
	public void start(Stage primaryStage) {
		startClient(new String("127.0.0.1"),9000);  //�������ڸ��� ���� ����
		
		// Draw Scene
	    setChatScene();
	    setSignUpScene();
	    setLobbyScene();
	    setLoginScene();
	           
	    window.show();
	}
	
	//���α׷��� ������
	public static void main(String[] args) {
		launch(args);
	}
}
