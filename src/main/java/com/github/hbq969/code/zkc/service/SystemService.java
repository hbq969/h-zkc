package com.github.hbq969.code.zkc.service;

import com.github.hbq969.code.zkc.dao.entity.MenuEntity;
import com.github.hbq969.code.zkc.dao.entity.RoleEntity;
import com.github.hbq969.code.zkc.dao.entity.RoleMenuEntity;
import com.github.hbq969.code.zkc.dao.entity.UserEntity;
import com.github.hbq969.code.zkc.model.UserInfo;
import com.github.hbq969.code.zkc.view.request.LoginInfo;
import com.github.hbq969.code.zkc.view.request.PasswordModify;
import com.github.pagehelper.PageInfo;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * @author : hbq969@gmail.com
 * @description : 系统管理业务接口
 * @createTime : 2025/1/2 11:05
 */
public interface SystemService {
    PageInfo<RoleEntity> queryRoleList(int pageNum, int pageSize, RoleEntity q);

    void saveRoleEntity(RoleEntity entity);

    void updateRoleEntity(RoleEntity entity);

    @Transactional(rollbackFor = Exception.class)
    void deleteRoleEntity(Long id);

    List<Map> queryRoleMenus(Long id);

    PageInfo<UserEntity> queryUserList(int pageNum, int pageSize, UserEntity q);

    void saveUserEntity(UserEntity entity);

    void updateUserEntity(UserEntity entity);

    void deleteUserEntity(Long id);

    void updatePassword(PasswordModify passwordModify);

    PageInfo<MenuEntity> queryMenuList(int pageNum, int pageSize, MenuEntity q);

    void saveMenuEntity(MenuEntity entity);

    void updateMenuEntity(MenuEntity entity);

    void deleteMenuEntity(Long id);

    @Transactional(rollbackFor = Exception.class)
    void updateRoleMenus(RoleMenuEntity roleMenuEntity);

    void login(LoginInfo login, HttpServletRequest request, HttpServletResponse response);

    void logout(HttpServletRequest request, HttpServletResponse response);

    com.github.hbq969.code.sm.login.model.UserInfo getUserInfo(HttpServletRequest request);

    HttpSession getSession(String sid);
}
