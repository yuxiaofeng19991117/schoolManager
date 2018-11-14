package club.pinea.school.shiro.aop;

import java.lang.reflect.Method;

import javax.naming.NoPermissionException;

import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import club.pinea.school.common.exception.NoUserException;
import club.pinea.school.config.JedisCacheConfig;
import club.pinea.school.model.SysUser;
import club.pinea.school.service.JedisService;
import club.pinea.school.shiro.annotation.Permission;
import club.pinea.school.util.ShiroUtil;

@Aspect
@Component
public class PermissionAop {
	
	private Logger log = LoggerFactory.getLogger(PermissionAop.class);
	
	@Autowired
	private JedisService jedisService;
	
	@Pointcut(value = "@annotation(club.pinea.school.shiro.annotation.Permission)")
	private void cutPermission() {
	}

	//拦截带有@Permission注解的请求
	@Around("cutPermission()")
	public Object doPermission(ProceedingJoinPoint point) throws Throwable {
		MethodSignature ms = (MethodSignature) point.getSignature();
		Method method = ms.getMethod();
		Permission permission = method.getAnnotation(Permission.class);
		Object permissions = permission.value();
		// 如果没有权限验证直接通过
		if (permissions == null) {
			point.proceed();
		}
		SysUser jedisUser = jedisService.getUser(JedisCacheConfig.USER_PREFIX.getMsg(point.getArgs()[0]+""));
		SysUser user = ShiroUtil.getUser();
		if(jedisUser == null){
			throw new NoUserException();
		}
		//redis中有用户数据，shiro中没有，以redis中数据为准，重新认证
		else if(user == null) {
			jedisService.authUser(JedisCacheConfig.USER_PREFIX.getMsg(point.getArgs()[0]+""));
		}
		//更新用户过期时间
		jedisService.updateUserExpiryTime(JedisCacheConfig.USER_PREFIX.getMsg(point.getArgs()[0]+""));
		//判断是否含有该角色code
		if (!ShiroUtil.hasRole(permissions.toString())) {
			throw new NoPermissionException();
		}
		return point.proceed();
	}
	
}
