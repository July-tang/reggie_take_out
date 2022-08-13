package com.july.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.july.reggie.commom.R;
import com.july.reggie.commom.ValidateCodeUtils;
import com.july.reggie.entity.User;
import com.july.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @author july
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送验证码
     *
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)) {
            String code = ValidateCodeUtils.generateValidateCode(6);
            log.info("发出的手机验证码为：{}", code);
            session.setAttribute(phone,code);
            return R.success("手机验证码短信发送成功");
        }
        return R.error("手机号为空，短信发送失败");
    }

    /**
     * 登陆
     *
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map<String, String> map, HttpSession session) {
        String phone = map.get("phone");
        String code = map.get("code");

        Object codeInSession = session.getAttribute(phone);

        if (codeInSession != null && codeInSession.equals(code)) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);
            if (user == null) {
                //新用户
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                user.setName(phone);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
        return R.error("登陆失败");
    }

    /**
     * 用户退出登陆
     *
     * @param session
     * @return
     */
    @PostMapping("/loginout")
    public R<String> loginout(HttpSession session){
        //清理Session中保存的当前用户登录的id
        session.removeAttribute("user");
        return R.success("退出成功");
    }
}
