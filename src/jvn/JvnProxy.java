package jvn;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import enums.MethodType;

public class JvnProxy implements InvocationHandler {

	private JvnObject jvnObject;

	public JvnProxy(JvnObject jo) throws JvnException{
		this.jvnObject = jo;

		try {
			jo.jvnUnLock();
		} catch (JvnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Object newInstance(Serializable obj, String name) throws JvnException {
		JvnServerImpl jvnServerImpl = JvnServerImpl.jvnGetServer();
		JvnObject jvnObject = null;

		try {
			jvnObject = jvnServerImpl.jvnLookupObject(name);
			if (jvnObject == null) {
				jvnObject = jvnServerImpl.jvnCreateObject(obj);
				jvnServerImpl.jvnRegisterObject(name, jvnObject);
			}
		} catch (JvnException e) {
			e.printStackTrace();
		}

		return Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(),
				new JvnProxy(jvnObject));
	}

	// Interception of the method invocations to lock and unlock the shared object
	@Override
	public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
		Object result = null;

		try {
			if (method.isAnnotationPresent(MethodType.class)) {
				String type = method.getAnnotation(MethodType.class).type();
				System.out.println("type =========> "+type);
				if (type.equals("read")) {
					jvnObject.jvnLockRead();
				} else if (type.equals("write")) {
					jvnObject.jvnLockWrite();
				} else {
					throw new JvnException("error in type method!!");
				}
			}
			
			System.out.println("***********error");
			result = method.invoke(jvnObject, args);
			jvnObject.jvnUnLock();
		} catch (JvnException e) {
			e.getMessage();	
		}

		return result;
	}

}
