/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Implementation of a Jvn server
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class JvnServerImpl extends UnicastRemoteObject implements JvnLocalServer, JvnRemoteServer {

	private JvnRemoteCoord coord;
	public HashMap<Integer, JvnObject> jvnObjsCache = null;
	private  int maxCacheSize = 3;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// A JVN server is managed as a singleton
	private static JvnServerImpl js = null;

	/**
	 * Default constructor
	 * 
	 * @throws JvnException
	 **/
	public JvnServerImpl() throws Exception {
		super();
		jvnObjsCache = new HashMap<Integer, JvnObject>();

		try {
			// get the coordinator reference through the naming service
			this.coord = (JvnRemoteCoord) Naming.lookup("rmi://localhost:1099/COORD");
			System.out.println("Reference coordinator retrieved!!!");
		} catch (Exception e) {
			e.getMessage();
		}

	}

	/**
	 * Static method allowing an application to get a reference to a JVN server
	 * instance
	 * 
	 * @throws JvnException
	 **/
	public static JvnServerImpl jvnGetServer() {
		if (js == null) {
			try {
				js = new JvnServerImpl();
			} catch (Exception e) {
				return null;
			}
		}
		return js;
	}

	/**
	 * The JVN service is not used anymore
	 * 
	 * @throws JvnException
	 **/
	public void jvnTerminate() throws jvn.JvnException {
		try {
			coord.jvnTerminate(this);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JvnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * creation of a JVN object
	 * 
	 * @param o : the JVN object state
	 * @throws JvnException
	 **/
	public JvnObject jvnCreateObject(Serializable o) throws jvn.JvnException {
		return new JvnObjectImpl(o);
	}

	/**
	 * Associate a symbolic name with a JVN object
	 * 
	 * @param jon : the JVN object name
	 * @param jo  : the JVN object
	 * @throws JvnException
	 **/
	public void jvnRegisterObject(String jon, JvnObject jo) throws jvn.JvnException {
		if(jvnObjsCache.size() >= maxCacheSize){
			throw new JvnException("Maximum cache size achieved");
		}		
		if (jo != null) {
			try {
				int idObject = coord.jvnGetObjectId();
				jo.setJvnObjectId(idObject);
				coord.jvnRegisterObject(jon, jo, this);
				//add jvn object in cache
				jvnObjsCache.put(idObject, jo);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * Provide the reference of a JVN object beeing given its symbolic name
	 * 
	 * @param jon : the JVN object name
	 * @return the JVN object
	 * @throws JvnException
	 **/
	public JvnObject jvnLookupObject(String jon) throws jvn.JvnException {
		JvnObject jvnObject = null;
		try {
			jvnObject = coord.jvnLookupObject(jon, this);
			if (jvnObject != null) {
				if(jvnObjsCache.size() >= maxCacheSize){
					throw new JvnException("Maximum cache size achieved");
				}
				jvnObjsCache.put(jvnObject.jvnGetObjectId(), jvnObject);
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jvnObject;
	}

	/**
	 * Get a Read lock on a JVN object
	 * 
	 * @param joi : the JVN object identification
	 * @return the current JVN object state
	 * @throws JvnException
	 **/
	public Serializable jvnLockRead(int joi) throws JvnException {
		try {
			return coord.jvnLockRead(joi, this);
		} catch (Exception e) { 
			throw new JvnException("Error lock read!!");
		}
	}

	/**
	 * Get a Write lock on a JVN object
	 * 
	 * @param joi : the JVN object identification
	 * @return the current JVN object state
	 * @throws JvnException
	 **/
	public Serializable jvnLockWrite(int joi) throws JvnException {
		try {
			return coord.jvnLockWrite(joi, this);
		} catch (Exception e) {
			throw new JvnException("Error lock write!!!");
		}
	}

	/**
	 * Invalidate the Read lock of the JVN object identified by id called by the
	 * JvnCoord
	 * 
	 * @param joi : the JVN object id
	 * @return void
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public void jvnInvalidateReader(int joi) throws java.rmi.RemoteException, jvn.JvnException {
		JvnObject jvnObject = jvnObjsCache.get(joi);
		jvnObject.jvnInvalidateReader();
	};

	/**
	 * Invalidate the Write lock of the JVN object identified by id
	 * 
	 * @param joi : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public Serializable jvnInvalidateWriter(int joi) throws java.rmi.RemoteException, jvn.JvnException {
		// to be completed
		JvnObject jvnObject = jvnObjsCache.get(joi);
		return jvnObject.jvnInvalidateWriter();
	};

	/**
	 * Reduce the Write lock of the JVN object identified by id
	 * 
	 * @param joi : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public Serializable jvnInvalidateWriterForReader(int joi) throws java.rmi.RemoteException, jvn.JvnException {
		// to be completed
		JvnObject jvnObject = jvnObjsCache.get(joi);
		return jvnObject.jvnInvalidateWriterForReader();	
	};

}
