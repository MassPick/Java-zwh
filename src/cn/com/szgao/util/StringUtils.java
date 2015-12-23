package cn.com.szgao.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import net.sf.json.JSONArray;
//import net.sf.json.JSONObject;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;

/**
 * 
 * 
 * 项目名称：MassPick 类名称：StringUtils 类描述： 处理字符串类 创建人：liuming 创建时间：2015-9-1
 * 上午11:16:26 修改人：liuming 修改时间：2015-9-1 上午11:16:26 修改备注：
 * 
 * @version
 * 
 */
public class StringUtils {
	private static Logger log = LogManager.getLogger(StringUtils.class);

	/**
	 * 半角转全角
	 * 
	 * @param input
	 *            String.
	 * @return 全角字符串.
	 */
	public static String toSBC(String input) {

		char c[] = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == ' ') {
				c[i] = '\u3000';
			} else if (c[i] < '\177') {
				c[i] = (char) (c[i] + 65248);

			}
		}
		return new String(c);
	}

	/**
	 * 全角转半角
	 * 
	 * @param input
	 *            String.
	 * @return 半角字符串
	 */
	public static String toDBC(String input) {

		char c[] = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == '\u3000') {
				c[i] = ' ';
			} else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
				c[i] = (char) (c[i] - 65248);

			}
		}
		String returnString = new String(c);

		return returnString;
	}

	/**
	 * JAVA自带的函数 判断是否为数字
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		for (int i = str.length(); --i >= 0;) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 判断 字符串是不是数字，包含小数
	 * @param str
	 * @return
	 * @author liuming
	 * @Date 2015-11-4 上午11:02:40
	 */
	public static boolean isNumericDecimal(String str) {
		
		if (str != null && "" != str) {
			String regex = "^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$";
			Matcher matcher = Pattern.compile(regex).matcher(str);
			return matcher.matches();
		} else {
			return false;
		}
	}

	/**
	 * 去除全角或半角的空格
	 * 
	 * @param str
	 * @author liuming
	 * @Date 2015-9-14 下午12:04:59
	 */
	public static String removeBlank(String str) {
		if (null != str) {
			return str.replaceAll("[ |　|]", " ").replace(" ", "");
		}
		return str;
	}

	/**
	 * 判断某字符串中包含的某字符的个数 不能处理空格" "，也不能用trim()
	 * 
	 * @param str
	 *            要处理的字符串 c 分割的字符
	 * @return
	 * @author liuming
	 * @Date 2015-9-14 上午11:40:55
	 */
	public static int countCharacter(String str, String c) {
		int count = 0;
		if (StringUtils.isNull(str)) {
			return 0;
		} else {
			String[] ary = (" " + str + " ").split(c);
			count = ary.length - 1;
		}

		return count;
	}

	/**
	 * 效率较高的一种判断字符串为空的方法 为空：true 不为空:false
	 * 
	 * @param: @param str
	 * @param: @return
	 * @return: Boolean 为空：true 不为空:false
	 * @author liuming
	 * @Date 2015-9-1 上午11:14:10
	 * @throws
	 */
	public static Boolean isNull(String str) {

		if (str == null || str.length() <= 0) {
			return true;
		}
		return false;
	}

	public static Boolean isCardId(String cardId) {
		// 定义判别用户身份证号的正则表达式（要么是15位，要么是18位，最后一位可以为字母）
		// 430623198808296435 430623 19880829 6435 15位: 430623670401643 430623
		// 67 0401 643

		// String str15=
		// "^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$" ;
		// String str18=
		// "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9]|X)$"
		// ;

		// "((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])" 0829

		Pattern idNumPattern = Pattern
				.compile("(\\d{14}[0-9a-zA-Z])|(\\d{17}[0-9a-zA-Z])");
		// 通过Pattern获得Matcher
		Matcher idNumMatcher = idNumPattern.matcher(cardId);
		// 判断用户输入是否为身份证号
		if (idNumMatcher.matches()) {
			return true;
		}
		return false;
	}

	/**
	 * 去除字符串中的数字
	 * 
	 * @param str
	 * @return
	 */
	public static String removeNum(String str) {
		return str.replaceAll("\\d+", "");
	}

	/**
	 * 去字符串中3 个到3 个以上的数字字组
	 * 
	 * @param str
	 * @return
	 * @author liuming
	 * @Date 2015-9-1 下午5:51:44
	 */
	public static String removeNum3(String str) {
		return str.replaceAll("\\d{3,}", "");
	}

	/**
	 * 用正则表达式 判断是否为数字
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumber(String str) {

		if (str != null && "" != str) {
			str = str.replace(" ", "");
			Pattern pattern = Pattern.compile("[0-9]+");
			return pattern.matcher(str).matches();
		} else {
			return false;
		}

	}

	/**
	 * 用正则表达式 判断是否为汉字
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isChinese(String str) {
		if (str != null && "" != str) {
			String regex = "([\u4e00-\u9fa5]+)";
			Matcher matcher = Pattern.compile(regex).matcher(str);
			return matcher.matches();
		} else {
			return false;
		}

	}

	private final static Pattern patternCardId = Pattern
			.compile("(\\d{14}[0-9a-zA-Z])|(\\d{17}[0-9a-zA-Z])");

	// private final static Pattern patternCardId15 =
	// Pattern.compile("(\\d{14}[0-9a-zA-Z])");
	// private final static Pattern patternCardId18 =
	// Pattern.compile("(\\d{17}[0-9a-zA-Z])");

	/**
	 * 提取字符串中的一个身份证 或全是18位的多个
	 * 
	 * @param str
	 * @return
	 */
	public static String pickUpCardId(String str) {

		Matcher matcher = patternCardId.matcher(str);

		StringBuffer bf = new StringBuffer();
		while (matcher.find()) {
			bf.append(matcher.group()).append(",");
		}

		int len = bf.length();
		if (len > 0) {
			bf.deleteCharAt(len - 1);
		}
		return bf.toString();
	}

	/**
	 * 提取字符串中的多个身份证
	 * 
	 * @param str
	 * @return
	 */
	public static String pickUpManyCardId(String str) {

		StringBuffer bf = new StringBuffer();
		List<Term> lists = HanLP.segment(str);
		for (Term term : lists) {

			if (term.nature == Nature.m) {
				Matcher matcher = patternCardId.matcher(term.word);
				if (matcher.matches()) {
					bf.append(term.word).append(",");
				}
			}
		}

		int len = bf.length();
		if (len > 0) {
			bf.deleteCharAt(len - 1);
		}
		return bf.toString();
	}

	/**
	 * 将字符串中特殊的字符转化为空格 " "
	 * 
	 * @param str
	 * @return
	 */
	public static String subSpeCharBlank(String str) {
		str = str.replace("-", " ").replace("-", " ").replace("，", " ")
				.replace(",", " ");
		return str;
	}

	/**
	 * 从身份证得性别
	 * 
	 * @param idCard
	 * @return
	 */
	public static String getSexFromIdCard(String idCard) {
		String sex = null;
		try {

			if (idCard.length() == 18) {
				sex = idCard.substring(16, 17);
			} else if (idCard.length() == 15) {
				sex = idCard.substring(14, 15);
			} else {
				return sex;
			}
			if (isNumber(sex)) {
				if (Integer.parseInt(sex) % 2 == 0) {
					sex = "女";
				} else {
					sex = "男";
				}
			} else {
				sex = null;
			}
		} catch (Exception e) {
//			ExceptionUtils.saveException(idCard, e);
			log.info("Exception  -------------------------------------------------------------------------------------------  "
					+ e);
			// log.debug("debug--------------------------------  "+ e);
		}
		return sex;

	}

	/**
	 * 判断是否为汉字
	 * 
	 * @param str
	 * @return
	 */
	public boolean isChinese2(String str) {

		char[] chars = str.toCharArray();
		boolean isGB2312 = false;
		for (int i = 0; i < chars.length; i++) {
			byte[] bytes = ("" + chars[i]).getBytes();
			if (bytes.length == 2) {
				int[] ints = new int[2];
				ints[0] = bytes[0] & 0xff;
				ints[1] = bytes[1] & 0xff;

				if (ints[0] >= 0x81 && ints[0] <= 0xFE && ints[1] >= 0x40
						&& ints[1] <= 0xFE) {
					isGB2312 = true;
					break;
				}
			}
		}
		return isGB2312;
	}

	/**
	 * 用ascii码 判断是否为数字
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumerAciic(String str) {
		for (int i = str.length(); --i >= 0;) {
			int chr = str.charAt(i);
			if (chr < 48 || chr > 57)
				return false;
		}
		return true;
	}

	/**
	 * 判断一个字符串的首字符是否为字母
	 * 
	 * @param s
	 * @return
	 */
	public static boolean test(String s) {
		char c = s.charAt(0);
		int i = (int) c;
		if ((i >= 65 && i <= 90) || (i >= 97 && i <= 122)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean check(String fstrData) {
		char c = fstrData.charAt(0);
		if (((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断字符串是否包含中文 (无法判断中文的字符)
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isHasHanZi(String str) {

		String regEx = "[\u4e00-\u9fa5]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		while (m.find()) {
			return true;
		}
		return false;
	}

	/**断字符串是否包含中文 (可法判断中文的字符)
	 * st.getBytes().length来判断，如果为2，就是中文的，这个对中文符号，。！￥、都好使
	 * 
	 * @param str
	 */
	public static boolean isHasHanziSpeChar(String str) {
		if (str.getBytes().length == str.length()) {
			return false;
		} else {
			return true;
		}

	}
	/**
	 * 判断字符串的每一位都是相同的
	 * @param str
	 * @return
	 */
	public static boolean isSameChars(String str) {
//		System.out.println(str);
//		str=toDBC(str);
//		System.out.println(str);
		if (str.length() < 2){
			return true;
		}
		char first = str.charAt(0);
		for (int i = 1; i < str.length(); i++){
			if (str.charAt(i) != first)
				return false;
		}
		return true;
	}
	
	/**
	 * 全角符号转半角
	 * @param str
	 * @return
	 * @author liuming
	 * @Date 2015-10-10 下午12:31:21
	 */
	public static String QtoB(String str) {
		String[] regsQ = { "！", "，",  "。",  "；","【","】"};
		String[] regsB = { "!",  ",",   ".",    ";" ,"[","]"};
		for ( int i = 0; i < regsQ.length ; i++ )
		{
		    str = str.replaceAll (regsQ[i], regsB[i]);
		}
		return str;
	}
	/**
	 * 将特殊字符转为""
	 * @param str
	 * @return
	 * @author liuming
	 * @Date 2015-10-16 上午10:50:03
	 */
	public static String QtoNull(String str) {
		
		String[] regsQ = { "！", "，",  "。",  "；","【","】","`","!","★","？","©㊣·","＋","№"};
		for ( int i = 0; i < regsQ.length ; i++ )
		{
		    str = str.replaceAll (regsQ[i], "");
		}
		return str;
	}
	
	public static String QtoNullCourt(String str) {
		String[] regsQ = { "！", "，",  "。",  "；","【","】","`","!","★","？","©㊣·","＋","№","?","、","－","!","-","／","/","¿¬ìå","￥","﹤","﹥","<",">","\\","、"};
		for ( int i = 0; i < regsQ.length ; i++ )
		{
			if("?".equals(regsQ[i])){
				str = str.replaceAll ("\\?", "");
				continue;
			}
		    str = str.replaceAll (regsQ[i], "");
		}
		return str;
	}
	
	/**
	 * 去特殊符号
	 * @param str
	 * @return
	 * @author liuming
	 * @Date 2015-10-29 下午6:36:03
	 */
	public static String removeSpecialCharacters(String str){
		if(null==str){
			return null;
		}
		str = str.replaceAll( "[©㊣№¿¬ìå¿¬ìå★�]" , "");
		return str;
	}
	
	/**
	 * 去标点符号
	 * @param str
	 * @return
	 * @author liuming
	 * @Date 2015-10-29 下午6:27:59
	 */
	public static String removePunctuation(String str){
		if(null==str){
			return null;
		}
		str = str.replaceAll( "[\\p{P}+~$`^=|<>～｀＄＾＋＝｜＜＞￥×]" , "");
		return str;
	}

	/**
	 * 根据URL得到返回的ID集合
	 * @param url
	 * @return
	 * @author liuming
	 * @Date 2015-10-15 下午5:38:09
	 */
//	public static List<String> getKeysFromBC(String url){
//		List<String> list=new ArrayList<>();
//		String json = loadJSON(url);
//		JSONObject jsonObj = JSONObject.fromObject(json);
//		JSONArray jsonArray = jsonObj.getJSONArray("rows");
//		if (null != jsonArray && jsonArray.length() > 0) {
//			for (int i = 0; i < jsonArray.length(); i++) {
//				Object obj = jsonArray.get(i);
//				JSONObject json2 = JSONObject.fromObject(obj);
//				list.add(json2.getString("id"));
//			}
//		}
//		return list;
//	}

	public static String loadJSON(String url) {
		StringBuilder json = new StringBuilder();
		try {
			URL oracle = new URL(url);
			URLConnection yc = oracle.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					yc.getInputStream()));
			String inputLine = null;
			while ((inputLine = in.readLine()) != null) {
				json.append(inputLine);
			}
			in.close();
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
		return json.toString();
	}
	
	public static void main(String[] args) {
		
		System.out.println(StringUtils.isSameChars("【["));
		
		String str = "强力清除广告，上网更快的浏览器！搞什么飞机嘛。我去；【";
		 
		System.out.println (StringUtils.QtoB(str));
		
//		System.out.println(StringUtils.isChinese("无"));
//		System.out.println(StringUtils.isHasHanziSpeChar("中11"));
//		System.out.println(StringUtils.isHasHanziSpeChar("！33"));

//		System.out.println(StringUtils.isNumeric("2141A4"));

		// String testStr = "　　西式灯饰受欢迎 尽情演绎奢华味道";
		// // testStr = testStr.replaceAll("[ |　]", " ").trim();
		// testStr = testStr.trim();
		//
		// System.out.println(testStr.replace(" ", ""));
		//
		// if("".equals("　".trim())){
		// System.out.println("半全相等");
		// }

		// String str = ""+",ab|cd||fdf!!sabcd//fd/sf ,abc fdfda()bc(abc,"+"" ;
		// String [] ary = ("" + str + "").split(",");
		// for (String string : ary) {
		// System.out.println("-----"+string);
		// }
		// System.out.println("ABC的个数 : " + (ary.length - 1));
		//
		// System.out.println(StringUtils.countCharacter(str, "fd"));
		//
		// String tempDepartment="青州市人民法院）".replace("（",
		// "(").replace("）", ")");
		// if( ( StringUtils.countCharacter(tempDepartment,
		// "\\(")==1&&StringUtils.countCharacter(tempDepartment, "\\)")==0 ) ){
		// tempDepartment = tempDepartment.replace("(", "");
		// }
		// if(( StringUtils.countCharacter(tempDepartment,
		// "\\)")==1&&StringUtils.countCharacter(tempDepartment, "\\(")==0 )){
		// tempDepartment = tempDepartment.replace(")", "");
		// }
		// System.out.println(tempDepartment);

		// 1. 用正则表达式处理, 不过好像一点都不省事..
		// Pattern p = Pattern.compile(",",Pattern.CASE_INSENSITIVE);
		// Matcher m = p.matcher(str);
		// int count = 0;
		// while(m.find()){
		// count ++;
		// }
		// System.out.println("ABC的个数 : " + count);

		// System.out.println(StringUtils.pickUpCardId("刘晴天 220104197209251817 15143027777 ")
		// .replace(",", " "));
		// System.out.println(StringUtils.getSexFromIdCard("1326011988085401"));
		// System.out.println(StringUtils.isNumber("1 "));
		// System.out.println(StringUtils.pickUpManyCardId("刚志松132601198808296543、陈怀龙 332601198808296543 432601198808296543 ").replace(",",
		// " "));

		// String []
		// gatherthes="刘晴天 22010419_7209\\251817, safd-AAA,: BBB".split("[0-9,\\-,:,_,:,\\\\]");
		//
		// for (String string : gatherthes) {
		// if(null!=string&&!"".equals(string)){
		// System.out.print(string+"\n");//"©"
		// }
		// }

		// String address="上海^上海市@闵行区#吴中|路";
		// // String[] splitAddress=address.split("\\^|@|#|\\|");
		// String[] splitAddress=address.split("[\\^,@,\\#,\\|]");
		// for (String string : splitAddress) {
		// System.out.println(string);
		// }

		// System.out.println(splitAddress[0]+splitAddress[1]+splitAddress[2]+splitAddress[3]);

		/*
		 * StringUtils su = new StringUtils();
		 * System.out.println(su.isCardId("4306236704016432"));
		 * 
		 * System.out.println(StringUtils.isCardId("4306236704016432"));
		 */
	}

}
