package club.pinea.school.shiro.aop;

import java.lang.reflect.Method;
import java.util.Map;

import javax.naming.NoPermissionException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import club.pinea.school.model.SysUser;
import club.pinea.school.service.JedisService;
import club.pinea.school.shiro.annotation.Permission;
import club.pinea.school.util.ShiroUtil;

@Aspect
@Component
public class PermissionAop {
	
	@Autowired
	private JedisService jedisService;
	
	@Pointcut(value = "@annotation(club.pinea.school.shiro.annotation.Permission)")
	private void cutPermission() {
	}

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
		SysUser user = ShiroUtil.getUser();
		if(user==null){
			user = jedisService.authUser(point.getArgs()[0]+"");
			if(user == null)
			throw new NoPermissionException();
		}
		//判断是否含有该角色code
		if (!ShiroUtil.hasRole(permissions.toString())) {
			throw new NoPermissionException();
		}
		return point.proceed();
	}
	
}
