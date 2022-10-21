package jvn;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import jvn2.MethodType;
import jvn2.SentenceImpl;

public class JvnProxy implements InvocationHandler {

	private JvnObject jvnObject;

	public JvnProxy(JvnObject jo) throws JvnException {
		this.jvnObject = jo;
	}

	public static Object newInstance(Serializable obj, String name) throws JvnException {
		JvnObject jo = null;
		try {
			// initialize JVN
			JvnServerImpl js = JvnServerImpl.jvnGetServer();
			jo = js.jvnLookupObject("IRC");

			if (jo == null) {
				jo = js.jvnCreateObject((Serializable) new SentenceImpl());
				// after creation, I have a write lock on the object
				jo.jvnUnLock();
				js.jvnRegisterObject("IRC", jo);
			}
		} catch (JvnException e) {
			e.getMessage();
		}

		return Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(),
				new JvnProxy(jo));
	}

	// Interception of the method invocations to lock and unlock the shared object
	@Override
	public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
		Object result = null;

		try {
			if (method.isAnnotationPresent(MethodType.class)) {
				String type = method.getAnnotation(MethodType.class).type();
				System.out.println("type =========> " + type);
				if (type.equals("read")) {
					jvnObject.jvnLockRead();
				} else if (type.equals("write")) {
					jvnObject.jvnLockWrite();
				} else {
					throw new JvnException("error in type method!!");
				}
			}

			Serializable temp = jvnObject.getCurrentJvnObject();
			result = method.invoke(temp, args);

			jvnObject.jvnUnLock();
		} catch (JvnException e) {
			e.getMessage();
		}

		return result;
	}

}
