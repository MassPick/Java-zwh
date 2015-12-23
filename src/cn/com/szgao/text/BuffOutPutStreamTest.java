package cn.com.szgao.text;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BuffOutPutStreamTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			readDataToFile();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static Connection conn_a = null;
	private static PreparedStatement pstmt_a = null;
	private static ResultSet rs_a = null;

	private static String FilePath = "";
	private static File fileName = null;
	private static FileOutputStream fos;
	private static BufferedOutputStream bos;
	private static String fileAllPath = "";
	static Map map = new HashMap();

	// public void init(){
	// try {
	// Class.forName("oracle.jdbc.driver.OracleDriver");
	//
	// map = PublicUtil.readConfigFile();
	//
	// conn_a =
	// DriverManager.getConnection((String)map.get("URL"),(String)map.get("USERNAME"),(String)map.get("PASSWORD"));
	//
	// FilePath = PublicUtil.readServerPath();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// }

	/**
	 * 链接postgresql
	 * 
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static Connection getConnection() throws ClassNotFoundException,
			SQLException {
		String url = "jdbc:postgresql://192.168.1.2:5432/duplicatedb";
		String usr = "postgres";
		String psd = "615601.xcy*";
		Connection conn = null;
		Class.forName("org.postgresql.Driver");
		conn = DriverManager.getConnection(url, usr, psd);
		return conn;
	}

	public static void readDataToFile() throws ClassNotFoundException, SQLException {
		long totalStart = System.currentTimeMillis();
		getConnection();
		try {
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			String nowDateStr = sdf.format(date);
			fileAllPath = FilePath + nowDateStr + ".txt";
			fileName = new File(fileAllPath);

			try {
				fos = new FileOutputStream(fileName);
				bos = new BufferedOutputStream(fos);
			} catch (IOException e) {
				e.printStackTrace();
			}
			createFile("");
			// 读配置文件--取SQL
			String sql_a = (String) map.get("SQL");
			System.out.println(sql_a);
			pstmt_a = conn_a.prepareStatement(sql_a);
			rs_a = pstmt_a.executeQuery();
			int num = 0; // 记录写文件写了多少行
			while (rs_a.next()) {
				long startTime = System.currentTimeMillis();
				String size = (String) map.get("SIZE");
				String s = "";
				for (int i = 1; i <= Integer.parseInt(size); i++) {
					s += rs_a.getString(i) + "|";
				}
				s = s.substring(0, s.length() - 1);
				createFile(s);
				num++;
				long endTime = System.currentTimeMillis();
				System.out.println("写入文件第" + num + "行，耗时"
						+ (endTime - startTime) + "毫秒.");

				if (num >= 1000000) {
					break;
				}
				// -----------定量清缓存一次，如果数据量大（上百万），请开启这个机制
				if (num == 0) {
					System.out.println("===============清缓存一次===========");
					try {
						bos.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				// -----------清缓存机制 end--------------------------------------
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			finish(); // 关闭输入流
			closeDB();
		}
		long totalEnd = System.currentTimeMillis();
		System.out.println("----总耗时：" + (totalEnd - totalStart) + "毫秒");
	}

	public static void createFile(String s) {
		try {
			bos.write(s.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void finish() {// 关闭输入流，将文字从缓存写入文件
		try {
			bos.flush();
			bos.close();
			fos.close();
		} catch (IOException iox) {
			System.err.println(iox);
		}
	}

	public static void closeDB() { // 关闭数据库连接
		if (rs_a != null) {
			try {
				rs_a.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (pstmt_a != null) {
					try {
						pstmt_a.close();
					} catch (SQLException e) {
						e.printStackTrace();
					} finally {
						if (conn_a != null) {
							try {
								conn_a.close();
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}
}
