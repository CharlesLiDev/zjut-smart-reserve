package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.NoticeMsg;
import com.example.entity.SysAdmin;
import com.example.mapper.NoticeMsgMapper;
import com.example.mapper.SysAdminMapper;
import com.example.service.SysAdminService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class SysAdminServiceImpl extends ServiceImpl<SysAdminMapper, SysAdmin> implements SysAdminService {

    private final SysAdminMapper sysAdminMapper;
    private final NoticeMsgMapper noticeMsgMapper;

    // JWT配置（和场地管理员一致）
    private final String JWT_SECRET = "campusVenueAdmin2026";
    private final long JWT_EXPIRE = 3600000L;

    @Override
    public String login(String username, String password) {
        // 1. 查询超级管理员
        SysAdmin sysAdmin = sysAdminMapper.selectByUsername(username);
        if (sysAdmin == null) {
            throw new RuntimeException("账号不存在");
        }
        // 2. 验证状态
        if (sysAdmin.getStatus() == 0) {
            throw new RuntimeException("账号已禁用");
        }
        // 3. 验证密码（MD5）
        String encryptPwd = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        if (!encryptPwd.equals(sysAdmin.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        // 4. 生成JWT
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(sysAdmin.getSysAdminId().toString())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRE))
                .signWith(key)
                .compact();
    }

    @Override
    public boolean publishNotice(String title, String content, Integer sysAdminId) {
        // 简化版：实际需推送给所有用户，这里模拟插入公告
        NoticeMsg msg = new NoticeMsg();
        msg.setReceiverType(0); // 0代表全局
        msg.setReceiverId(0);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setSendBy(sysAdminId);
        return noticeMsgMapper.insert(msg) > 0;
    }

    @Override
    public boolean sendMsg(Integer receiverType, Integer receiverId, String title, String content, Integer sendBy) {
        NoticeMsg msg = new NoticeMsg();
        msg.setReceiverType(receiverType);
        msg.setReceiverId(receiverId);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setSendBy(sendBy);
        return noticeMsgMapper.insert(msg) > 0;
    }
}