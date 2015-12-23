package cn.com.szgao.action;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.google.gson.Gson;

import cn.com.szgao.dto.ArchivesVO;
import cn.com.szgao.util.CommonConstant;

public class LocalToServer {
	private static Cluster cluster = CouchbaseCluster.create("192.168.1.4");
	private static Cluster cluster2 = CouchbaseCluster.create("192.168.1.13");
	static int count = 0 ;
	static Logger logger = LogManager.getLogger(LocalToServer.class.getName());
	static  String[] ERCOEDING={"й","෨","Ժ","ۼ","ҩ","ල","ɷ","ص","δ","ġ","Ϊ","ط","Ϣ","ȡ","Ӫ","ã","","Դ","ڲ","Ѱ","�"};
	/**
	 * 桶数据迁移到其他桶
	 * @param args
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		PropertyConfigurator.configure("F:\\work\\WorkSpace_Eclipse\\WorkSpace_Eclipse\\MassPick\\WebContent\\WEB-INF\\log4j.properties");
		Bucket bucket = null;
		Bucket bucket2 = null;
		try {
			bucket = connectionBucket(bucket);
			bucket2  = connectionBucket2(bucket2);
			insert(bucket,bucket2);
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			bucket.close();
			bucket2.close();
			cluster = null;
			cluster2 = null ;
		}
	}
	//连接查询CB中的桶
	private static Bucket connectionBucket(Bucket bucket){
		try {
			bucket = connectionCouchBaseLocal();//本地CB
		} catch (Exception e) {
			while(true){	
				try{
					bucket = connectionCouchBaseLocal();//本地CB
					break;
				}
				catch(Exception ee){
					Log.error(ee);
				}
			}
		}
		return bucket;
	}
	
	//连接写入CB中的桶
	private static Bucket connectionBucket2(Bucket bucket2){
		try {
			bucket2 = connectionCouchBase();//本地CB
		} catch (Exception e) {
			while(true){	
				try{
					bucket2 = connectionCouchBase();//本地CB
					break;
				}
				catch(Exception ee){
					Log.error(ee);
				}
			}
		}
		
		return bucket2;
	}
	
	public static void insert(Bucket bucket,Bucket bucket2) throws Exception{
		 List<ArchivesVO> archlist = new ArrayList<ArchivesVO>();
		 ArchivesVO arch = null;
		Connection conn=getConnection();	
		conn.setAutoCommit(false); //当值为false时，sql命令的提交由应用程序负责，程序必须调用commit或者rollback方法；值为true时，sql命令的提交（commit）由驱动程序负责    
		 Statement st = conn.createStatement();
		 String sql = "SELECT URL_ID FROM filed_zwh_test1";//查询SQL
		 ResultSet rs = st.executeQuery(sql);
		 int i = 0 ;
		 while (rs.next()){
			 arch = new ArchivesVO();
			 	count ++;
				arch.setUuid(rs.getString(1));//获取ID
				archlist.add(arch);
				if(archlist.size()>=1000){
					i++;//每一千条，批次加1
					boolean result = update(archlist,bucket,bucket2);//操作成功返回true，否则返回false
					logger.info("第"+i+"批数据："+archlist.size());
					if (!result) {
						logger.info("更新1失败"+rs.getString(1));
						
					}
					archlist = null;//赋空值，释放资源
					archlist = new ArrayList<ArchivesVO>();
				}
			   }
		 if (null != archlist && archlist.size() > 0) {
			 i++;//不满一千条数据作为一个批次
				boolean result = update(archlist,bucket,bucket2);//操作成功返回true，否则返回false
				logger.info("第"+i+"批数据："+archlist.size());
					if (!result) {
						logger.info("更新2失败"+rs.getString(1));
						
					}
					archlist = null;//赋空值，释放资源
					archlist = new ArrayList<ArchivesVO>();
				}
	}
	
	/**
	 * 对couchbase进行操作
	 * @param list 对象集合
	 * @param bucket
	 * @return
	 * @throws Exception
	 */
	public static boolean update(List<ArchivesVO> list,Bucket bucket,Bucket bucket2) throws Exception {
		if(null==list||list.size()<=0){
			return false;
		}
		JsonDocument doc=null;
		JsonObject obj=null;	
		com.google.gson.JsonObject json=null;
		Gson gson=new Gson();
		ArchivesVO archs = null;
		try{
		for(ArchivesVO arch : list){
	 	doc = JsonDocument.create(arch.getUuid());
		obj =bucket.get(doc)==null?null:bucket.get(doc).content();
		
		if(obj == null){
			logger.info("匹配不到UUID:"+arch.getUuid());
				continue;	
			}
		archs = new ArchivesVO();
		json=gson.fromJson(obj.toString(), com.google.gson.JsonObject.class);
		archs = gson.fromJson(json, ArchivesVO.class);
		if(null != obj.get("title") && !"".equals(obj.get("title"))){
			archs.setTitle(obj.get("title").toString());//获取CB中该条数据的标题
		}
		if(null != obj.get("detailLink")&& !"".equals(obj.get("detailLink"))){
			archs.setDetailLink(obj.get("detailLink").toString());//获取CB中该条数据的URL
		}
		if(null != obj.get("catalog") && !"".equals(obj.get("catalog"))){
			archs.setCatalog(obj.get("catalog").toString());//
		}
		if(null != obj.get("caseNum") && !"".equals(obj.get("caseNum"))){
//			archs.setCaseNum(full2HalfChange(obj.get("caseNum").toString()));//获取CB中该条数据的案号
			archs.setCaseNum(replaceAllCaseNum(obj.get("caseNum").toString()));//获取CB中该条数据的案号
		}
		if(null != obj.get("courtName") && !"".equals(obj.get("courtName"))){
			archs.setCourtName(obj.get("courtName").toString());//获取CB中该条数据的法院名
		}
		if(null != obj.get("publishDate") && !"".equals(obj.get("publishDate"))){
			archs.setPublishDate(getReplaceAllDate(obj.get("publishDate").toString().trim()));//获取CB中该条数据的审结日期
		}
		if(null != obj.get("province") && !"".equals(obj.get("province"))){
			archs.setProvince(obj.get("province").toString());//获取CB中该条数据的省份名
		}
		if(null != obj.get("city") && !"".equals(obj.get("city"))){
			archs.setCity(obj.get("city").toString());//获取CB中该条数据的
		}
		if(null != obj.get("area") && !"".equals(obj.get("area"))){
			archs.setArea(obj.get("area").toString());//获取CB中该条数据的县/区
		}
		if(null != obj.get("collectDate") && !"".equals(obj.get("collectDate"))){
			archs.setCollectDate(getReplaceAllDate(obj.get("collectDate").toString().trim()));//获取CB中该条数据的采集日期
		}
		if(null != obj.get("plaintiff") && !"".equals(obj.get("plaintiff"))){
			archs.setPlaintiff(obj.get("plaintiff").toString());//获取CB中该条数据的PDF
		}
		if(null != obj.get("defendant") && !"".equals(obj.get("defendant"))){
			archs.setDefendant(obj.get("defendant").toString());//获取CB中该条数据的被告
		}
		if(null != obj.get("approval") && !"".equals(obj.get("approval"))){
			archs.setApproval(obj.get("approval").toString());//获取CB中该条数据的审批结果
		}
		if(null != obj.get("suitType") && !"".equals(obj.get("suitType"))){
			archs.setSuitType(obj.get("suitType").toString());//获取CB中该条数据的诉讼类型
		}
		if(null != obj.get("suitDate") && !"".equals(obj.get("suitDate"))){
			archs.setSuitDate(obj.get("suitDate").toString());//获取CB中该条数据的起诉日期
		}
		if(null != obj.get("approvalDate") && !"".equals(obj.get("approvalDate"))){
			archs.setApprovalDate(obj.get("approvalDate").toString());//获取CB中该条数据的审结日期
		}
		if(null != obj.get("caseCause") && !"".equals(obj.get("caseCause"))){
			archs.setCaseCause(obj.get("caseCause").toString());//获取CB中该条数据的案由
		}
		if(null != obj.get("summary") && !"".equals(obj.get("summary"))){
			archs.setSummary(obj.get("summary").toString());//获取CB中该条数据的摘要
		}
			String jsonss=gson.toJson(archs);
			doc = JsonDocument.create(arch.getUuid(),JsonObject.fromJson(jsonss));
			bucket2.upsert(doc);	
		}
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}
		finally{
			archs = null;
			gson = null;
			json = null;
			obj=null;
			doc=null;
			
		}
		return true;
	}
	
	//判断是否存在乱码
    public static boolean getErrorCode(String value){
    	if(value == null || "".equals(value)){return false;}
    	for(String val:ERCOEDING){
    		int index = value.lastIndexOf(val);
    		if(index <= 0){
    			continue;
    		}
    		return false;
    	}
    	return true;
    }
	
	/**
	 * 案号清洗
	 * @param value
	 * @return
	 */
	public static String replaceAllCaseNum(String value){
		if(value == null && "".equals(value)){return null;}
			value=value.replaceAll("[(,（,〔,【]","("); 
			value=value.replaceAll("[),）,﹞,】]",")");
//			value = value.replaceAll("]", ")");
//			value = value.replaceAll("[", "(");
			value=value.trim();
			return value;
	}
	
	/**
	 * 统一日期格式
	 * @param value
	 * @return
	 */
	 public static String getReplaceAllDate(String value){
		 StringBuffer sb=null;				  
		   if(value!=null&&!"".equals(value)){
			   //去掉特殊字符
			    value=value.replaceAll("[日,（,）,(,),【,】,{,},<,>]","");
			    value=value.replaceAll("[-,-,/,\",年,月]","-");
			    value=value.replaceAll("[:,：]", ":");
//			    value = value.replaceAll("]", "");
//				value = value.replaceAll("[", "");
			    value=value.trim();
				sb=new StringBuffer();
				sb.append(value);
		   }
		 return sb==null?null:sb.toString();
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
	
	public static Bucket connectionCouchBase(){
		//连接指定的桶		
		return cluster.openBucket("court",1,TimeUnit.MINUTES);	
	} 
	public static Bucket connectionCouchBaseLocal(){
		//连接指定的桶		
		return cluster2.openBucket("court",1,TimeUnit.MINUTES);	
	}
	
	
}
