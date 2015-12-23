package cn.com.szgao.action;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.google.gson.Gson;

/**
 * 抓取HTML写入couchbase库
 * 
 * @author Administrator
 */
public class HtmlToTxt {
	static long count = 0;// 总数量
	static long ERRORSUM = 0;// 出错数据
	static long SUM = 0;
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
	private static Logger logger = LogManager.getLogger(ExtractionHtml.class
			.getName());

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {

		File filepor = new File(
				"D:\\Company_File\\log4j-1215\\Java10\\batchImport.log");
		if (filepor.exists()) {
			filepor.delete();// 删除日志文件
		}
		filepor = null;
		PropertyConfigurator
				.configure("F:\\work\\WorkSpace_Eclipse\\WorkSpace_Eclipse\\MassPick\\WebContent\\WEB-INF\\log4j.properties");
		long da = System.currentTimeMillis();
		File file = new File("F:/DataSheet/DetailedPage/NoticeHtm/法院公告/法院公告HTML/4月28号之前");
		try {
			show(file);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			file = null;
		}
		logger.info(count + ":数量"+"----出错数据："+ERRORSUM);
		logger.info("所有文件总耗时"
				+ (((System.currentTimeMillis() - da) / 1000) / 60) + "分钟");
	}

	/**
	 * 递归遍历html文件
	 * 
	 * @param file
	 * @throws
	 * @throws Exception
	 */
	private static void show(File file) throws Exception {
		String html = null;
		Document doc;
		int i = 0;
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
					html = doc.body().text().trim();
					boolean Garbled = getErrorCode(html);// 判断编码是否错误
					if (Garbled == false) {
						logger.info(val + "编码错误！！！");
						i++;
						if (i == 5) {
							html = null;
						}
						continue;
					}
					i = 0;
					break;
				}
				if (html == null || "".equals(html)) {
					logger.info("内容为空的HTML页面：" + file.getPath());
				}
				int index1 = html.lastIndexOf("公告内容");
				int index2 = html.lastIndexOf("下载打印本公告");
				if(index1 <= 0 || index2 <= 0){
					ERRORSUM ++ ;
					return ;
				}
				html = html.substring(index1, index2);
				logger.info("法院ID："+ file.getName().substring(0,file.getName().lastIndexOf(".")));
				logger.info("所有内容：" + html);
				createFileTxt(html,file.getName().substring(0,file.getName().lastIndexOf(".")));
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
					// logger.info("网址:" + fi.getPath());
					for (String val : charset) { // 匹配不同编码格式
						doc = Jsoup.parse(fi, val);
						html = doc.body().text();
						boolean Garbled = getErrorCode(html);// 判断编码是否错误
						if (Garbled == false) {
							i++;
							if (i == 5) {
								html = null;
							} // 判断编码格式都不匹配的时候赋予空值
							continue;
						}
						i = 0;
						break;
					}
					if (html == null || "".equals(html)) {
						logger.info("内容为空的HTML页面：" + fi.getPath());
						continue;
					}
					int index1 = html.lastIndexOf("公告内容");
					int index2 = html.lastIndexOf("下载打印本公告");
					if(index1 == -1 || index2 == -1){
						ERRORSUM ++ ;
						logger.info("UUID："+ fi.getPath());
						continue ;
					}
					html = html.substring(index1, index2);
					createFileTxt(html,fi.getName().substring(0,fi.getName().lastIndexOf(".")));// 生成文件，并写入公告内容
				} else if (fi.isDirectory()) {
					logger.info(fi.getName());
					show(fi);
				} else {
					continue;
				}
			}
	}

	// 判断是否存在乱码
	public static boolean getErrorCode(String value) {
		if (value == null || "".equals(value)) {
			return false;
		}
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
	 */
	public static void createFileTxt(String value, String uuid) {
		FileOutputStream outStream = null;
		try {
			count++;
			outStream = new FileOutputStream("D:\\Notice\\4月28号之前\\" + uuid + ".txt");
			Gson gson = new Gson();
			outStream.write(gson.toJson(value).toString().getBytes());
			logger.info("第:" + count + "数据ID为：" + uuid);
		} catch (Exception e) {
			logger.error(e);
		} finally {
			try {
				if (null != outStream)
					outStream.close();
				outStream = null;
			} catch (IOException e) {
				logger.error(e);
			}
		}

	}
}
