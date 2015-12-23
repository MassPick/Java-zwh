package cn.com.szgao.action;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jfree.util.Log;

import cn.com.szgao.dto.ArchivesVO;
import cn.com.szgao.util.CommonConstant;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.google.gson.Gson;

/**
 * 从postgreSql中获取detailLink写入到couchbase
 * @author Administrator
 *
 */
public class CBToPGDetailLink {
	private static Logger logger = LogManager.getLogger( CBToPGDetailLink.class.getName());
	static Map<String,String> UrlMap = new HashMap<String,String>();//detailLink
	static long count = 0 ;
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		File filepor = new File("E:\\Company_File\\log4j-1118\\Java1\\batchImport.log");
		if (filepor.exists()) {
			filepor.delete();// 删除日志文件
		}
		filepor = null;
		PropertyConfigurator.configure("F:\\work\\WorkSpace_Eclipse\\WorkSpace_Eclipse\\MassPick\\WebContent\\WEB-INF\\log4j.properties");
		long da = System.currentTimeMillis();
		Bucket bucket = null;
		bucket = connectionBucket(bucket);
		listUrl(bucket);
		logger.info(count + ":数量");
		logger.info("所有文件总耗时" + (((System.currentTimeMillis() - da) / 1000) / 60) + "分钟");
	}
	//连接CB
		private static Bucket connectionBucket(Bucket bucket){
			try {
				bucket = CommonConstant.connectionCouchBase();//连接服务器CB
//				bucket = CommonConstant.connectionCouchBaseLocal();//连接本地CB
			} catch (Exception e) {
				while(true){	
					try{
						bucket = CommonConstant.connectionCouchBase();
						break;
					}
					catch(Exception ee){
						Log.error(ee);
					}
				}
			}
			
			return bucket;
		}
		
		
		/**
		 * 查询url
		 * @throws Exception 
		 */
		public static void listUrl(Bucket bucket) throws Exception
		{
			List<ArchivesVO> list = new ArrayList<ArchivesVO>();
			ArchivesVO arch = null;
			arch = new ArchivesVO();
			PreparedStatement urlStmt = null;//查询
			Connection conn = null;//连接
			ResultSet rs = null;//结果集
			String UrlSql = "select url_id,url from extract_url_t_copy2 where url_id = 'f205485d-db18-503a-9c79-e9740f26a6b2'";//url
			try {
				conn = getConnection();	
				urlStmt = conn.prepareStatement(UrlSql);	//预编译查询
				rs = urlStmt.executeQuery();//查询
				while(rs.next())
				{
					arch.setUuid(rs.getString(1));
					arch.setDetailLink(rs.getString(2));
					logger.info("UUID:"+rs.getString(1)+"-----URL:"+rs.getString(2));
					list.add(arch);
//				if(list.size()>=1000){
					boolean result =  updateJsonData(list,bucket);
					if (result){
						count++;
						logger.info("更新成功"+count);
					}
					list = null;
					list = new ArrayList<ArchivesVO>();
//				}
				}	
			} catch (ClassNotFoundException e) {			
				e.printStackTrace();
			} catch (SQLException e) {			
				e.printStackTrace();
			}finally{
			      try{
			          if(null !=urlStmt)
			          {
			        	  urlStmt.close();		        
			        	  urlStmt = null;		        		          
			         }
			      }catch(SQLException se){
			    }
			      try{
			         if(conn!=null)
			            conn.close();
			      }catch(SQLException se){
			         se.printStackTrace();
			      }
			   }
		}
		
		/**
		 * 裁判文书 抓取word，HTML修改court桶
		 */
		public static boolean updateJsonData(List<ArchivesVO> list, Bucket bucket) throws Exception {
			if (null == list || list.size() <= 0) {
				return false;
			}
			JsonDocument doc = null;
			JsonObject obj2 = null;
			com.google.gson.JsonObject json = null;
			Gson gson = new Gson();
			ArchivesVO archs = null;
			try {
				for (ArchivesVO arch : list) {
					// 查询数据
					doc = JsonDocument.create(arch.getUuid()); // 获取ID
					obj2 = bucket.get(doc) == null ? null : bucket.get(doc).content();
					if (obj2 == null) {
						logger.info("匹配不到UUID:" + arch.getUuid());
						continue;
					}
					archs = new ArchivesVO();
					json = gson.fromJson(obj2.toString(),
							com.google.gson.JsonObject.class);
					archs = gson.fromJson(json, ArchivesVO.class);

					if (null != obj2.get("title") && !"".equals(obj2.get("title"))) {
						archs.setTitle(obj2.get("title").toString());// 标题
					}
					if (null != obj2.get("caseNum")
							&& !"".equals(obj2.get("caseNum"))) {
						archs.setCaseNum(obj2.get("caseNum").toString());// 案号
					}
					if (null != obj2.get("courtName") && !"".equals(obj2.get("courtName"))) {
						archs.setCourtName(obj2.get("courtName").toString());// 法院名
					}
					if (null != obj2.get("approval") && !"".equals(obj2.get("approval"))) {
						archs.setApproval(obj2.get("approval").toString());// 审批结果
					}
					if (null != obj2.get("caseCause") && !"".equals(obj2.get("caseCause"))) {
						archs.setCaseCause(obj2.get("caseCause").toString());// 案由
					}
					if (null != obj2.get("catalog") && !"".equals(obj2.get("catalog"))) {
						archs.setCatalog(obj2.get("catalog").toString());// 分类
					}
					if (null != obj2.get("plaintiff") && !"".equals(obj2.get("plaintiff"))) {
						archs.setPlaintiff(obj2.get("plaintiff").toString());// 原告
					}
					if (null != obj2.get("defendant") && !"".equals(obj2.get("defendant"))) {
						archs.setDefendant(obj2.get("defendant").toString());// 被告
					}
					if (null != obj2.get("approvalDate") && !"".equals(obj2.get("approvalDate"))) {
						archs.setApprovalDate(obj2.get("approvalDate").toString());// 审结日期
					}
					if (null != obj2.get("summary") && !"".equals(obj2.get("summary"))) {
						archs.setSummary(obj2.get("summary").toString());// 摘要
					}
					if (null != obj2.get("detailLink") && !"".equals(obj2.get("detailLink"))) {
						archs.setDetailLink(obj2.get("detailLink").toString());// url
					}
					if (null != arch.getDetailLink() && !"".equals(arch.getDetailLink())) {
						archs.setDetailLink(arch.getDetailLink());// url
					}
					if (null != obj2.get("publishDate") && !"".equals(obj2.get("publishDate"))) {
						archs.setPublishDate(ExtractionHtml.getReplaceAllDate(obj2.get("publishDate").toString()));// 发布日期
					}
					if (null != obj2.get("province") && !"".equals(obj2.get("province"))) {
						archs.setProvince(obj2.get("province").toString());// 省
					}
					if (null != obj2.get("city") && !"".equals(obj2.get("city"))) {
						archs.setCity(obj2.get("city").toString());// 市
					}
					if (null != obj2.get("area") && !"".equals(obj2.get("area"))) {
						archs.setArea(obj2.get("area").toString());// 县
					}
					if (null != obj2.get("collectDate") && !"".equals(obj2.get("collectDate"))) {
						archs.setCollectDate(ExtractionHtml.getReplaceAllDate(obj2.get(
								"collectDate").toString()));// 采集时间
					}
					if (null != obj2.get("suitDate") && !"".equals(obj2.get("suitDate"))) {
						archs.setSuitDate(obj2.get("suitDate").toString());// 起诉日期
					}
					String jsonss = gson.toJson(archs);
					doc = JsonDocument.create(arch.getUuid(),
							JsonObject.fromJson(jsonss));
//					logger.info(doc);
					bucket.upsert(doc);
				}
			} catch (Exception e) {
				logger.info(e.getMessage());
				return false;
			} finally {
				gson = null;
				json = null;
				archs = null;
				obj2 = null;
				doc = null;
			}
			return true;
		}
		
		/**
		 * 链接postgresql
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
