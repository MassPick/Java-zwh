package cn.com.szgao.action;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
/**
 * 裁判文书HTML抽取标签内容
 * @author DELL
 *
 */
public class HtmlToHtml {
	static long count = 0;// 总数量
	private static Logger logger = LogManager.getLogger(HtmlToHtml.class
			.getName());
	static Map<String, String> MAPS = new HashMap<String, String>();
	static {
		MAPS.put("html", "html");
		MAPS.put("htm", "htm");
		MAPS.put("txt", "txt");
	}
	public static String[] charset = { "utf-8", "gbk", "gb2312", "gb18030",
			"big5" };
	public static String[] ERCOEDING = { "й", "෨", "Ժ", "ۼ", "ҩ", "ල", "ɷ",
			"ص", "δ", "ġ", "Ϊ", "ط", "Ϣ", "ȡ", "Ӫ", "ã", "", "Դ", "ڲ", "Ѱ",
			"�" };
	
	public static void main(String[] args) throws Throwable {
		PropertyConfigurator.configure("F:\\work\\WorkSpace_Eclipse\\WorkSpace_Eclipse\\MassPick\\WebContent\\WEB-INF\\log4j.properties");
		long da = System.currentTimeMillis();
		File file = new File("G:\\Data\\十二月\\HTML\\zgcpwsw-20151127");
		try {
			first(file);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			file = null;
			logger.info("所有文件总耗时"+ (((System.currentTimeMillis() - da) / 1000) / 60 ) + "分钟");
		}
	}

	public static void first(File file) throws Throwable {
		Document doc = null;
			if (file.isFile()) {
				String suffix = file.getName();
				suffix = suffix.substring(suffix.indexOf(".") + 1,
						suffix.length());
				suffix = MAPS.get(suffix);
				if (null == suffix) {
					return;
				}
				logger.info("网址:" + file.getPath());
				for (String val : charset) {// 匹配不同编码格式
					doc = Jsoup.parse(file, val);
					boolean Garbled = getErrorCode(doc);// 判断编码是否错误
					if (Garbled == false) {
						logger.info(val + "编码错误！！！");
						continue;
					}
					break;
				}
				Elements listDiv = doc.getElementsByAttributeValue("id","PrintArea");// 检索ID为wenshu的标签
				if(listDiv == null){
					logger.info("--------HTML中没有找到标签："+file.getPath());
					return;
				}
				createFileTxt(listDiv, file);
//				logger.info(listDiv);
				return;
			}
			File[] files = file.listFiles();
			for (File fi : files) {
				if (fi.isFile()) {
					String suffix = fi.getName();
					suffix = suffix.substring(suffix.indexOf(".") + 1,
							suffix.length());
					suffix = MAPS.get(suffix);
					if (null == suffix) {
						return;
					}
					for (String val : charset) {// 匹配不同编码格式
						doc = Jsoup.parse(fi, val);
						boolean Garbled = getErrorCode(doc);// 判断编码是否错误
						if (Garbled == false) {
							logger.info(val + "编码错误！！！");
							continue;
						}
						break;
					}
					Elements listDiv = doc.getElementsByAttributeValue("id","PrintArea");// 检索ID为wenshu的标签
					if(listDiv == null){
						logger.info("--------HTML中没有找到标签："+fi.getPath());
						return;
					}
					createFileTxt(listDiv, fi);
				}else if (fi.isDirectory()) {
						logger.info(fi.getName());
						first(fi);
					} else {
						continue;
					}
			}
	}

	// 判断是否存在乱码
	public static boolean getErrorCode(Document doc) {
		if (doc == null || "".equals(doc)) {
			return false;
		}
		String value = doc.text();
		for (String val : ERCOEDING) {
			int index = value.lastIndexOf(val);
			if (index <= 0) {
				continue;
			}
			return false;
		}
		return true;
	}

	/**
	 * 根据对象ID生成文件名
	 * 
	 * @param notice
	 * @throws Throwable 
	 */
	public static void createFileTxt(Elements value, File file) throws Throwable {
		FileWriter fw = null;
		BufferedWriter bw = null;
		String uuid = file.getName().substring(0, file.getName().lastIndexOf("."));
		try {
			count++;
			String str = "D"+file.getParent().substring(1);//获取文件目录，动态在D盘生成相对应的文件目录
			File files = new File(str);
			//如果文件夹不存在则创建    
			if  (!files .exists()  && !files .isDirectory())      
			{       
			    logger.info(files+"//不存在");  
			    files .mkdirs();  //如果该磁盘不存在此目录就自动生成，且可以生成多级目录
			}
			fw = new FileWriter(files +"/" + uuid + ".html", true);
			bw = new BufferedWriter(fw);
			bw.write("<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>");
			bw.write("<html xmlns='http://www.w3.org/1999/xhtml'>");
			bw.write("<meta http-equiv='Content-Type' content='text/html; charset=utf-8' />");
			bw.append(value.toString());
			bw.write("</html>");
			 logger.info("第:" + count + "数据ID为：" + uuid);
		} catch (Exception e) {
			logger.error(e);
		} finally {
			try {
				if (bw != null) {bw.close();}
				if (fw != null) {fw.close();}
			} catch (IOException e) {
				logger.error(e);
			}
		}

	}
	
	/**
	 * 生成多级目录
	 * @throws Throwable
	 */
	public static void route(File file) throws Throwable{
		String str = "D"+file.getParent().substring(1);
		File files = new File(str);
		//如果文件夹不存在则创建    
		if  (!files .exists()  && !files .isDirectory())      
		{       
		    logger.info(files+"//不存在");  
		    files .mkdirs();  
		} 
//		else   
//		{  
//			logger.info("//目录存在");  
//		} 
	}
}
