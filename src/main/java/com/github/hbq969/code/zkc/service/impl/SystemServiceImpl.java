package com.github.hbq969.code.zkc.service.impl;

import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.sm.login.session.UserContext;
import com.github.hbq969.code.zkc.config.ZkcProperties;
import com.github.hbq969.code.zkc.dao.SystemDao;
import com.github.hbq969.code.zkc.dao.entity.MenuEntity;
import com.github.hbq969.code.zkc.dao.entity.RoleEntity;
import com.github.hbq969.code.zkc.dao.entity.RoleMenuEntity;
import com.github.hbq969.code.zkc.dao.entity.UserEntity;
import com.github.hbq969.code.zkc.model.UserInfo;
import com.github.hbq969.code.zkc.service.SystemService;
import com.github.hbq969.code.zkc.view.request.LoginInfo;
import com.github.hbq969.code.zkc.view.request.PasswordModify;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author : hbq969@gmail.com
 * @description : 系统管理业务类
 * @createTime : 2025/1/2 11:05
 */
@Service("zkc-SystemServiceImpl")
@Slf4j
public class SystemServiceImpl implements SystemService, InitializingBean {

    @Autowired
    private SystemDao systemDao;

    @Autowired
    private SpringContext context;

    @Autowired
    private ZkcProperties conf;

    private Cache<String, HttpSession> sessions;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("配置的cookie、会话超时时间: {} 秒。", conf.getCookieMaxAgeSec());
        this.sessions = CacheBuilder.newBuilder()
                .maximumSize(500)
                .initialCapacity(100)
                .concurrencyLevel(10)
                .expireAfterAccess(conf.getCookieMaxAgeSec(), TimeUnit.SECONDS)
                .removalListener((RemovalListener<String, HttpSession>) notif -> {
                    log.info("session自动过期，sid: {}", notif.getKey());
                    notif.getValue().invalidate();
                }).build();
    }

    @Override
    public PageInfo<RoleEntity> queryRoleList(int pageNum, int pageSize, RoleEntity q) {
        if (pageNum < 0) {
            PageInfo<RoleEntity> pg = new PageInfo<>(systemDao.queryRoleList(q));
            return pg;
        } else {
            PageInfo<RoleEntity> pg = PageHelper.startPage(pageNum, pageSize).doSelectPageInfo(() -> systemDao.queryRoleList(q));
            pg.getList().forEach(e -> e.convertDict(context));
            return pg;
        }
    }

    @Override
    public void saveRoleEntity(RoleEntity entity) {
        entity.initial();
        systemDao.saveRoleEntity(entity);
    }

    @Override
    public void updateRoleEntity(RoleEntity entity) {
        entity.update();
        systemDao.updateRoleEntity(entity);
    }

    @Override
    public void deleteRoleEntity(Long id) {
        systemDao.deleteMenuEntities(id);
        systemDao.deleteUserEntities(id);
        systemDao.deleteRoleEntity(id);
    }

    @Override
    public List<Map> queryRoleMenus(Long id) {
        return systemDao.queryRoleMenus(id);
    }

    @Override
    public PageInfo<UserEntity> queryUserList(int pageNum, int pageSize, UserEntity q) {
        PageInfo<UserEntity> pg = PageHelper.startPage(pageNum, pageSize).doSelectPageInfo(() -> systemDao.queryUserList(q));
        pg.getList().forEach(e -> e.convertDict(context));
        return pg;
    }

    @Override
    public void saveUserEntity(UserEntity entity) {
        entity.initial(encoder);
        systemDao.saveUserEntity(entity);
    }

    @Override
    public void updateUserEntity(UserEntity entity) {
        entity.update();
        systemDao.updateUserEntity(entity);
    }

    @Override
    public void deleteUserEntity(Long id) {
        systemDao.deleteUserEntity(id);
    }

    @Override
    public void updatePassword(PasswordModify passwordModify) {
        UserEntity ue = systemDao.queryUserEntity(passwordModify.getId());
        if (ue == null) {
            throw new UnsupportedOperationException("用户不存在");
        }
        if (!encoder.matches(passwordModify.getOldPassword(), ue.getPassword())) {
            throw new IllegalArgumentException("老密码不对");
        }
        passwordModify.hash(encoder);
        systemDao.updateUserPassword(passwordModify);
    }

    @Override
    public PageInfo<MenuEntity> queryMenuList(int pageNum, int pageSize, MenuEntity q) {
        if (pageNum < 0) {
            List<MenuEntity> menus = systemDao.queryMenuList(q);
            PageInfo<MenuEntity> pg = new PageInfo<>(menus);
            pg.setTotal(menus.size());
            return pg;
        } else {
            PageInfo<MenuEntity> pg = PageHelper.startPage(pageNum, pageSize).doSelectPageInfo(() -> systemDao.queryMenuList(q));
            pg.getList().forEach(e -> e.convertDict(context));
            return pg;
        }
    }

    @Override
    public void saveMenuEntity(MenuEntity entity) {
        entity.initial();
        systemDao.saveMenuEntity(entity);
    }

    @Override
    public void updateMenuEntity(MenuEntity entity) {
        entity.update();
        systemDao.updateMenuEntity(entity);
    }

    @Override
    public void deleteMenuEntity(Long id) {
        systemDao.deleteMenuEntity(id);
    }

    @Override
    public void updateRoleMenus(RoleMenuEntity roleMenuEntity) {
        systemDao.deleteMenuEntities(roleMenuEntity.getRole().getId());
        context.getBean(JdbcTemplate.class).batchUpdate("insert into h_zkc_role_menus(role_id,menu_id) values(?,?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                MenuEntity me = roleMenuEntity.getMenus().get(i);
                ps.setLong(1, roleMenuEntity.getRole().getId());
                ps.setLong(2, me.getId());
            }

            @Override
            public int getBatchSize() {
                return roleMenuEntity.getMenus().size();
            }
        });
    }

    @Override
    public void login(LoginInfo login, HttpServletRequest request, HttpServletResponse response) {
        UserEntity user = systemDao.queryUserByName(login.getUsername());
        log.info("查询到用户信息: {}", user);
        if (encoder.matches(login.getPassword(), user.getPassword())) {
            log.info("密码验证一致");
            HttpSession session = request.getSession(true);
            // 创建会话对象
            UserInfo ui = new UserInfo();
            ui.setUserId(user.getId());
            ui.setUserName(user.getUsername());
            ui.setRoleId(user.getRoleId());
            ui.setRoleName(user.getRoleName());
            ui.setMenus(systemDao.queryRoleMenus(user.getRoleId()));
            session.setAttribute("user", ui);
            log.info("创建新会话: {}", session.getId());
            sessions.put(session.getId(), session);
        } else {
            throw new IllegalArgumentException("密码错误，请重试");
        }
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        if (session != null) {
            log.info("注销账号: {}, 会话: {}", session.getAttribute("user"), session.getId());
            sessions.invalidate(session.getId());
            Cookie jsessionCookie = new Cookie("JSESSIONID", null);
            jsessionCookie.setPath("/");
            jsessionCookie.setHttpOnly(true);
            response.addCookie(jsessionCookie);
        }
    }

    @Override
    public HttpSession getSession(String sid) {
        return sessions.getIfPresent(sid);
    }

    @Scheduled(fixedRate = 5000)
    void cleanExpiredSessions() {
        sessions.cleanUp();
    }

    @Override
    public com.github.hbq969.code.sm.login.model.UserInfo getUserInfo(HttpServletRequest request) {
        com.github.hbq969.code.sm.login.model.UserInfo ui = UserContext.get();
        return ui;
    }
}
