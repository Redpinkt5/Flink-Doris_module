package cn.rp.controller;

import cn.rp.dto.LoginRequest;
import cn.rp.result.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class LoginController {

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Result<Map<String, String>> login(@RequestBody LoginRequest request) {
        if ("admin".equals(request.getUsername()) && "123456".equals(request.getPassword())) {
            Map<String, String> data = new HashMap<>();
            data.put("token", UUID.randomUUID().toString().replace("-", ""));
            return Result.ok(data);
        }
        return Result.error(401, "用户名或密码错误");
    }
}
