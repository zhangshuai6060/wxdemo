package com.example.demo.utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Description: 微信相关工具类（获取token，获取用户信息...）
 */
public class ParentTokenUtil {

	private static final Logger log = LoggerFactory.getLogger(ParentTokenUtil.class);

	private static String access_token = "";
	private static String jsapi_ticket = "";

	//微信公众号的AppID
	public static String appId="wwdaweaewaweaweew";
	//微信公众号的AppSecret密钥
	public static String appSecret="ewweaweaweaweaweawe";

	// 获得微信登录token
	private static final String GET_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
			+ appId + "&secret=" + appSecret;

	/**
	 * 过期时间7200秒， 因为微信token过期时间为2小时，即7200秒 此处需要更改，不是所有的token都共享一个创建时间
	 */
	private static int expireTime = 7200 * 1000;
	private static long refreshTime;

	public static String generateVerifieURL(String ID) {
		String VERIFIE_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + appId
				+ "&uri=http%3a%2f%2fwww.sjzyxsj.com%2fzxyun%2fgetUserInfo&response_type=code&scope=snsapi_base&state="
				+ ID + "#wechat_redirect";
		return VERIFIE_URL;
	}

	public static String generateVerifieURL() {
		String VERIFIE_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + appId
				+ "&uri=http%3a%2f%2fwww.sjzyxsj.com%2fzxyun%2fgetUserInfo&response_type=code&scope=snsapi_base&state=STATE#wechat_redirect";
		return VERIFIE_URL;
	}

	/*
	 * 获取用户的Openid以及用户的accessToken（注意，此处accessToken不是微信token） 无需进行授权，仅用做本公众号唯一识别号
	 */


	//https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
	public static Map<String, String> getUserOpenID(String code) {
		String GET_USER_INFO_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + appId
				+ "&secret=" + appSecret + "&code=" + code + "&grant_type=authorization_code";
		String responseContent = HttpClientUtil.get(GET_USER_INFO_ACCESS_TOKEN_URL);
		JSONObject object = new JSONObject();
		try {
			object = new JSONObject(responseContent);
			Map<String, String> paras = new HashMap<String, String>();
			paras.put("openid", (String) object.get("openid"));
			paras.put("access_token", (String) object.get("access_token"));
			return paras;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * 获取用户的基本信息 无需进行授权，仅用做本公众号唯一识别号
	 */
	public static Map<String, String> getUserBasedInfo(String accessToken, String openID) {
		String GET_USER_INFO_URL = "https://api.weixin.qq.com/sns/userinfo?access_token=" + accessToken + "&openid="
				+ openID;
		String responseContent = HttpClientUtil.get(GET_USER_INFO_URL);
		JSONObject object = new JSONObject();
		try {
			object = new JSONObject(responseContent);
			Map<String, String> paras = new HashMap<String, String>();
			paras.put("openid", (String) object.get("openid"));
			paras.put("nickname", (String) object.get("nickname"));
			paras.put("headimgurl", (String) object.get("headimgurl"));
			return paras;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * 获取用户的Openid 无需进行授权，仅用做本公众号唯一识别号
	 */
	public static String getUserAccessToken(String code) {
		String GET_USER_INFO_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + appId
				+ "&secret=" + appSecret + "&code=" + code + "&grant_type=authorization_code";
		String responseContent = HttpClientUtil.get(GET_USER_INFO_ACCESS_TOKEN_URL);
		JSONObject object = new JSONObject();
		try {
			object = new JSONObject(responseContent);
			return (String) object.get("access_token");
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 获取微信accesstoken 默认返回false-不进行刷新操作
	 * 
	 * @return
	 */
	public static synchronized String getAccessToken() {
		return getAccessToken(false);
	}

	/*
	 * 获取微信accesstoken（当输入true时强制刷新accesstoken）
	 *
	 */
	public static synchronized String getAccessToken(boolean refresh) {
		// 三步判断-当前accesstoken为空，系统当前时间减去上次刷新时间大于过期时间，刷新标识符为真。
		if (!("").equals(access_token) || (System.currentTimeMillis() - refreshTime) > expireTime || refresh) {
			// 新建一个accesstoken
			access_token = initAccessToken();
			// 记录当前刷新时间
			refreshTime = System.currentTimeMillis();
		}

		return access_token;
	}

	private static String initAccessToken() {
		String responseContent = HttpClientUtil.get(GET_ACCESS_TOKEN_URL);
		JSONObject object = new JSONObject();
		try {
			object = new JSONObject(responseContent);
			return (String) object.get("access_token");
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static synchronized String getJsapiTicket() {
		return getJsapiTicket(false);
	}

	public static synchronized String getJsapiTicket(boolean refresh) {
		if (!("").equals(jsapi_ticket) || (System.currentTimeMillis() - refreshTime) > expireTime || refresh) {
			jsapi_ticket = initJsapiTicket(access_token);
			refreshTime = System.currentTimeMillis();
		}
		return jsapi_ticket;
	}

	public static String initJsapiTicket(String access_token) {
		String responseContent = HttpClientUtil
				.get("https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=" + access_token + "&type=jsapi");
		JSONObject object = new JSONObject();
		try {
			object = new JSONObject(responseContent);
			return (String) object.get("ticket");
		} catch (JSONException e) {
			try {
				System.out.println(
						"获取ticket失败 errcode:" + object.get("errcode") + " errmsg:" + object.getString("errmsg"));
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * 返回获得的签名
	 *
	 */
	public static Map<String, String> getSignature(String URL) {
		// 注意 URL 一定要动态获取，不能 hardcode
		String url = URL;
		Map<String, String> ret = sign(getJsapiTicket(), url);
		return ret;
	};

	/*
	 * 根据ticket和请求的url进行签名
	 *
	 */
	public static Map<String, String> sign(String jsapi_ticket, String url) {
		Map<String, String> ret = new HashMap<String, String>();
		String nonce_str = create_nonce_str();
		String timestamp = create_timestamp();
		String string1;
		String signature = "";

		// 注意这里参数名必须全部小写，且必须有序
		string1 = "jsapi_ticket=" + jsapi_ticket + "&noncestr=" + nonce_str + "&timestamp=" + timestamp + "&url=" + url;
		// System.out.println(string1);

		try {
			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			crypt.update(string1.getBytes("UTF-8"));
			signature = byteToHex(crypt.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		ret.put("url", url);
		ret.put("jsapi_ticket", jsapi_ticket);
		ret.put("nonceStr", nonce_str);
		ret.put("timestamp", timestamp);
		ret.put("signature", signature);

		return ret;
	}

	private static String byteToHex(final byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		String result = formatter.toString();
		formatter.close();
		return result;
	}

	private static String create_nonce_str() {
		return UUID.randomUUID().toString();
	}

	private static String create_timestamp() {
		return Long.toString(System.currentTimeMillis() / 1000);
	}
}
