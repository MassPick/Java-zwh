package cn.com.szgao.action;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jfree.util.Log;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import cn.com.szgao.dto.ArchivesVO;
import cn.com.szgao.util.CommonConstant;

public class Test_zwh {
	static int count = 0 ;
	static int sum = 0 ;
	static Logger logger = LogManager.getLogger(Test_zwh.class.getName());
	private static Cluster cluster = CouchbaseCluster.create("192.168.1.4");
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		File filepor = new File("E:\\Company_File\\log4j-1221\\pgIsNull\\batchImport.log");
		if (filepor.exists()) {
			filepor.delete();// 删除日志文件
		}
		filepor = null;
		PropertyConfigurator.configure("F:\\work\\WorkSpace_Eclipse\\WorkSpace_Eclipse\\MassPick\\WebContent\\WEB-INF\\log4j.properties");
		Bucket bucket = null;
		try {
			bucket = connectionBucket(bucket);
			insert(bucket);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			bucket.close();
			logger.info("extract_url_t3表总数据量："+sum+"----错误数据量："+count);
		}
	}
	//连接查询CB中的桶
	private static Bucket connectionBucket(Bucket bucket){
		try {
			bucket = connectionCouchBase();//本地CB
		} catch (Exception e) {
			while(true){	
				try{
					bucket = connectionCouchBase();//本地CB
					break;
				}
				catch(Exception ee){
					Log.error(ee);
				}
			}
		}
		
		return bucket;
	}
	
	
	public static void insert(Bucket bucket) throws Exception{
		Connection conn=getConnection();	
//		conn.setAutoCommit(false); //当值为false时，sql命令的提交由应用程序负责，程序必须调用commit或者rollback方法；值为true时，sql命令的提交（commit）由驱动程序负责    
		 Statement st = conn.createStatement();
		 String sql = "SELECT URL_ID FROM extract_url_t3";//查询SQL
		 ResultSet rs = st.executeQuery(sql);
		 JsonDocument doc=null;
			JsonObject obj=null;
			int index1 = 0 ;
			int index2 = 0 ;
		 try {
			 while (rs.next()){
				 index1++;
				 sum++;
					doc = JsonDocument.create(rs.getString(1));
					obj =bucket.get(doc)==null?null:bucket.get(doc).content();
					if(obj == null){
						count ++;
						logger.info("----"+count+"------匹配不到UUID:"+rs.getString(1));
						obj=null;
						doc=null;
						continue;	
						}
					if(index1 >= 1000){
						index2 ++ ;
					logger.info("--------"+index2+"--------");
					index1 = 0 ;
					}
					obj=null;
					doc=null;
					
				   }
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			conn.close();
			st.close();
			rs.close();
		}
	}
	
	public static Bucket connectionCouchBase(){
		//连接指定的桶		
		return cluster.openBucket("court",1,TimeUnit.MINUTES);	
	} 
	
	/**
	 * 连接postgreSql库
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static Connection getConnection() throws ClassNotFoundException,SQLException {
		String url = "jdbc:postgresql://192.168.1.2:5432/duplicatedb";
		String usr = "postgres";
		String psd = "615601.xcy*";
		Connection conn = null;
		Class.forName("org.postgresql.Driver");
		conn = DriverManager.getConnection(url, usr, psd);
		return conn;
	}
}
