package cn.yuheng.server.controller;

import cn.yuheng.server.dao.UserDao;
import cn.yuheng.server.pojo.User;
import cn.yuheng.server.util.PasswordUtil;
import cn.yuheng.server.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @author 王子陶
 * @version 1.0
 * @date 2020/4/5 下午7:18
 */
@RestController
public class UserController {

    @Autowired
    private UserDao userDao;

    public UserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * URI: /user/get
     * 参数：id
     * 测试专用
     */
    @RequestMapping(value = "/api/user/get", method = RequestMethod.GET)
    public Result<User> getUser(@RequestParam("id") Integer id) {
        User user = userDao.selectByID(id);
        return Result.successOrFail(user);
    }

    /**
     * URI /user/creat
     */
    @PostMapping(value = "/api/user/create/by-email")
    public Result createUser(@RequestBody Map<String, String> userJson) {
        String email = userJson.get("email");
        String password = userJson.get("password");
        if (email == null || password == null) {
            return Result.fail(null, "参数错误");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        if (null != userDao.selectByEmail(user.getEmail())) {
            return Result.fail(null, "邮箱账号已被注册");
        }
        int statusCode = userDao.insertSelective(user);
        return Result.successOrFail(statusCode == 1);
    }

    /**
     * @param session
     * @param email
     * @param password MD5Hex(MD5Hex(password+password[0:6]))+long(time))
     * @param time
     * @return
     */
    @RequestMapping(value = "/api/user/login/by-email", method = RequestMethod.POST)
    public Result<User> login(HttpSession session, @RequestParam("email") String email, @RequestParam("password") String password, Long time) {
        User user = userDao.selectByEmail(email);
        if (user == null) {
            return Result.fail();
        }
        if (PasswordUtil.checkPassword(password, time, user.getPassword())) {
            session.setAttribute("user", user);
            return Result.success(user);
        } else {
            return Result.fail();
        }
    }

    @RequestMapping(value = "/api/user/logout", method = RequestMethod.POST)
    public Result logout(HttpSession session) {
        session.removeAttribute("user");
        return Result.success();
    }
}
