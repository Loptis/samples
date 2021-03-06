package com.sample.shiro;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sample.shiro.infrastructure.domain.model.Permission;
import com.sample.shiro.infrastructure.domain.model.Role;
import com.sample.shiro.infrastructure.domain.model.User;
import com.sample.shiro.infrastructure.domain.repository.PermissionRepository;
import com.sample.shiro.infrastructure.domain.repository.RoleRepository;
import com.sample.shiro.infrastructure.domain.repository.UserRepository;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * 认证及授权
 */
@Component
public class UserRealm extends AuthorizingRealm {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionRepository permissionRepository;

    /**
     * 授权(验证权限时调用)
     * 为当前登陆成功的用户授予权限和角色，已经登陆成功了
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String userName = ((User) SecurityUtils.getSubject().getPrincipal()).getUsername();
        User user = userRepository.getOne(new QueryWrapper<User>().eq("username", userName));
        Role role = roleRepository.getOne(new QueryWrapper<Role>().eq("id", user.getRoleId()));
        Permission permission = permissionRepository.getOne(new QueryWrapper<Permission>().eq("role_id", user.getRoleId()));
        SimpleAuthorizationInfo info=new SimpleAuthorizationInfo();
        Set<String> roles = new HashSet<>();
        roles.add(role.getRolename());
        info.setRoles(roles);
        if (!ObjectUtils.isEmpty(permission)) {
            Set<String> permissions = new HashSet<>();
            permissions.add(permission.getPermissionName());
            info.setStringPermissions(permissions);
        }
        return info;
    }

    /**
     * 认证(登录时调用)
     * 验证当前登录的用户，获取认证信息
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String userName = token.getPrincipal().toString();
        User user = userRepository.getOne(new QueryWrapper<User>().eq("username", userName));
        if (ObjectUtils.isEmpty(user)) {
            throw new AuthenticationException("用户名错误");
        }
        return new SimpleAuthenticationInfo(user, user.getPassword(), ByteSource.Util.bytes(user.getSalt()), getName());
    }

}
