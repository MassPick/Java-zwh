package cn.com.szgao.action;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 * 通用连接数据库的方法
 * @param  conn: 连接对象
 * @author zwh
 * @since 2018-05-08
 * @version 1.0
 */
public class DataConnect {	
	public Connection getConnection() throws ClassNotFoundException, SQLException{
		String url = "jdbc:postgresql://192.168.1.2:5432/duplicatedb";
		String usr = "postgres";
		String psd = "615601.xcy*";
		Connection conn = null;
		Class.forName("org.postgresql.Driver");
		conn = DriverManager.getConnection(url, usr, psd);
		return conn;
	}
}
