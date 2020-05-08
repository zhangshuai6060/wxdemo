package com.example.demo.api;

import com.example.demo.utils.ParentTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping("api/bind")
public class BindApi {

	private static final Logger log = LoggerFactory.getLogger(BindApi.class);

	@Value("${weixin.appId}")
	String appId;

	@Value("${weixin.address}")
	String address;

	@RequestMapping("/getCode")
	private String getCode(HttpServletRequest request) {
		return "redirect:https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx5f3f084c7e1c7e2d&redirect_uri=http://www.xzzsedu.com/api/bind/enterPersonnelCenter&response_type=code&scope=snsapi_userinfo&state=caonima#wechat_redirect";
	}

	@RequestMapping("/enterPersonnelCenter")
	public String goPersonnelCenter(HttpServletRequest request, Model model) {
		String code = request.getParameter("code");
		// 根据code获取openid和accesstoken
		Map<String, String> paras = ParentTokenUtil.getUserOpenID(code);
		if(paras == null) {
			return "redirect:/api/bind/getCode";
		}
		String openid = paras.get("openid");
		//获取当前扫描人的信息
		log.error("openid=============================" + openid);
		Map<String, String> info = ParentTokenUtil.getUserBasedInfo(paras.get("access_token"), openid);
		log.error("headimgurl=============================" + info.get("headimgurl"));
		request.setAttribute("openid", openid);
		request.getSession().setAttribute("openid", openid);
		return "/success";
	}

}
