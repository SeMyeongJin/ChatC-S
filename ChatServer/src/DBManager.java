package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBManager {
	private static Connection con; // DB 연결 담당
	private static Statement st; // 쿼리문 작성
	private static ResultSet rs; // 쿼리문 결과 저장
	
	public static void initDB()
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://localhost:4500/chatdb", "root", "Jin03111004!");
			st = con.createStatement();
		}
		catch(Exception e)
		{
			System.out.println("Database error : " + e.getMessage());
		}
	}
	
	public static String login(String ID, String PW)
	{
		String str = new String();
		try
		{
			String SQL = "SELECT userName from userInfo where userID = '" + ID + "' and userPW = '" + PW + "'";
			rs = st.executeQuery(SQL);
			if (rs.next()) str = rs.getString("userName");
		}
		catch(Exception e)
		{
			System.out.println("database login error : " + e.getMessage());
		}
		return str;
	}
	
	public static boolean signup(String ID, String PW, String name)
	{
		try
		{
			String SQL = "Insert Into userInfo values ('" + ID + "', '" + PW +"', '" + name + "')";
			st.executeUpdate(SQL);
			return true;
		}
		catch(Exception e)
		{
			System.out.println("database signup error : " + e.getMessage());
		}
		return false;
	}
}
