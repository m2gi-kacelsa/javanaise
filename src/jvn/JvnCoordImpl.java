/***
 * JAVANAISE Implementation
 * JvnCoordImpl class
 * This class implements the Javanaise central coordinator
 * Contact:  
 *
 * Authors: 
 */

package jvn;

import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;

import enums.JvnObjectState;

public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord {

	/**
	 * 	
	 */
	private static final long serialVersionUID = 1L;
	private int currentId = -1;
	private HashMap<String, JvnObject> jvnObjectsNameMap = new HashMap<>();
	private HashMap<Integer, JvnObjectServersCouple> jvnObjectsOwnersMap = new HashMap<>();


	/**
	 * Default constructor
	 * 
	 * @throws JvnException
	 **/
	public JvnCoordImpl() throws Exception {

		LocateRegistry.createRegistry(1099);
		Naming.rebind("rmi://localhost:1099/COORD", this);
		System.out.println(" A REFERENCE OF THE COORDINATOR IN RMI REGISTRY IS CREATED!");

	}

	public static void main(String argv[]) throws Exception {
		JvnCoordImpl jvnCoordImpl = new JvnCoordImpl();
		System.out.println("Coordinator launched!!	" + jvnCoordImpl);

	}

	/**
	 * Allocate a NEW JVN object id (usually allocated to a newly created JVN
	 * object)
	 * 
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public synchronized int jvnGetObjectId() throws java.rmi.RemoteException, jvn.JvnException {
		// JvnObjectImpl jvnObject = new JvnObjectImpl();
		// je sais pas si serai mieux de synchroniser uniquement l'incrémentation et le
		// return ??
		return (++currentId);
	}

	/**
	 * Associate a symbolic name with a JVN object
	 * 
	 * @param jon : the JVN object name
	 * @param jo  : the JVN object
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public synchronized void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
			throws java.rmi.RemoteException, jvn.JvnException {

		if (jon == null) {
			throw new JvnException("jvn Name passed is null");
		} else if (jo == null) {
			throw new JvnException("jvnObject passed is null");
		} else if (js == null) {
			throw new JvnException("jvnServer passed is null");
		} else if (jo.jvnGetObjectId() < 0 || jo.jvnGetObjectId() > currentId) {
			throw new JvnException("Invalid jvnObject ID ! ");
		} else if (jvnObjectsNameMap.containsKey(jon)) {
			throw new JvnException("jvnObject with such a name is already registered !");
		} else if (jvnObjectsOwnersMap.containsKey(jo.jvnGetObjectId())) {
			throw new JvnException("Object with such an id is already registered !");
		}

		// To have trace of registred jvnObjects and severs having copy of such
		// jvnObject
		jvnObjectsOwnersMap.put(jo.jvnGetObjectId(), new JvnObjectServersCouple(jo, js, JvnObjectState.W));
		// To get better performance when looking up for jvnObject
		jvnObjectsNameMap.put(jon, jo);

	}

	/**
	 * Get the reference of a JVN object managed by a given JVN server
	 * 
	 * @param jon : the JVN object name
	 * @param js  : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException, jvn.JvnException {
		if (jon == null) {
			throw new JvnException("jvn Name passed is null");
		} else if (js == null) {
			throw new JvnException("jvnRemoteServer passed is null");
		}

		synchronized (this) {
			JvnObject searchedJvnObject = jvnObjectsNameMap.get(jon);
			if (searchedJvnObject == null) {
				return null;
			}
			return searchedJvnObject;
		}

	}

	/**
	 * Get a Read lock on a JVN object managed by a given JVN server
	 * 
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the server
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException, JvnException
	 **/
	public Serializable jvnLockRead(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
		if (js == null) {
			throw new JvnException("jvnRemoteServer passed is null");
		}

		synchronized (this) {
			JvnObjectServersCouple searchedJvnObjectServersCouple = jvnObjectsOwnersMap.get(joi);
			if (searchedJvnObjectServersCouple == null) {
				throw new JvnException(" Not found jvnObject with such an Id");
			} else if (searchedJvnObjectServersCouple.getJvnObjectMemberState() == JvnObjectState.NL) {
				searchedJvnObjectServersCouple.setJvnObjectMemberState(JvnObjectState.R);
				searchedJvnObjectServersCouple.getOwnerServers().add(js);
			} else if (searchedJvnObjectServersCouple.getJvnObjectMemberState() == JvnObjectState.R) {
				searchedJvnObjectServersCouple.getOwnerServers().add(js);

			} else if (searchedJvnObjectServersCouple.getJvnObjectMemberState() == JvnObjectState.W) {
				JvnRemoteServer ownerServer = searchedJvnObjectServersCouple.getOwnerServers().iterator().next();
				searchedJvnObjectServersCouple.setLatestJvnObjectContent(ownerServer.jvnInvalidateWriterForReader(joi));
				searchedJvnObjectServersCouple.setJvnObjectMemberState(JvnObjectState.R);
			}
			if (!searchedJvnObjectServersCouple.getOwnerServers().contains(js)) {
				searchedJvnObjectServersCouple.getOwnerServers().add(js);

			} else {
				throw new JvnException(
						"Unexpected lock state: " + searchedJvnObjectServersCouple.getJvnObjectMemberState() + " !");
			}
			return searchedJvnObjectServersCouple.getLatestJvnObjectContent();

		}


	}

	/**
	 * Get a Write lock on a JVN object managed by a given JVN server
	 * 
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the server
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException, JvnException
	 **/
	public Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
		if (js == null) {
			throw new JvnException("jvnRemoteServer passed is null !\n");
		}

		synchronized (this) {
			JvnObjectServersCouple searchedJvnObjectServersCouple = jvnObjectsOwnersMap.get(joi);
			if (searchedJvnObjectServersCouple == null) {
				throw new JvnException(" Not found jvnObject with such an Id !\n");
			} else if (searchedJvnObjectServersCouple.getJvnObjectMemberState() == JvnObjectState.NL) {
				// I could,'t figure out if there is something to do in this case
			} else if (searchedJvnObjectServersCouple.getJvnObjectMemberState() == JvnObjectState.R) {
				Iterator<JvnRemoteServer> ownerServersIterator = searchedJvnObjectServersCouple.getOwnerServers()
						.iterator();
				while (ownerServersIterator.hasNext()) {
					try {
						ownerServersIterator.next().jvnInvalidateReader(joi);

					} catch (Exception e) {
						throw new JvnException("Error invalidating a read lock!\n" + e);
					}

				}
			} else if (searchedJvnObjectServersCouple.getJvnObjectMemberState() == JvnObjectState.W) {
				try {
					Iterator<JvnRemoteServer> ownerServersIterator = searchedJvnObjectServersCouple.getOwnerServers()
							.iterator();
					searchedJvnObjectServersCouple
							.setLatestJvnObjectContent(ownerServersIterator.next().jvnInvalidateWriter(joi));

				} catch (Exception e) {
					throw new JvnException("Error invalidating a write lock!\n" + e);
				}
			}

			searchedJvnObjectServersCouple.getOwnerServers().clear();
			searchedJvnObjectServersCouple.getOwnerServers().add(js);
			searchedJvnObjectServersCouple.setJvnObjectMemberState(JvnObjectState.W);
			return searchedJvnObjectServersCouple.getLatestJvnObjectContent();

		}

	}

	/**
	 * A JVN server terminates
	 * 
	 * @param js : the remote reference of the server
	 * @throws java.rmi.RemoteException, JvnException
	 **/
	public void jvnTerminate(JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
		if (js == null) {
			throw new JvnException("jvnRemoteServer passed is null !\n");
		}

		synchronized (this) {
			Iterator<JvnObjectServersCouple> allJvnObjectServersIterator = jvnObjectsOwnersMap.values().iterator();
			Iterator<JvnRemoteServer> OwnerServersIterator;
			JvnObjectServersCouple currentJvnObjectServersCouple;
			JvnRemoteServer currentJvnServer;

			// for each registerd JvnObject we look up if is concerned by this server
			while (allJvnObjectServersIterator.hasNext()) {
				currentJvnObjectServersCouple = allJvnObjectServersIterator.next();
				OwnerServersIterator = currentJvnObjectServersCouple.getOwnerServers().iterator();

				// to do that we look up in all servers does own a copy of it
				while (OwnerServersIterator.hasNext()) {
					currentJvnServer = OwnerServersIterator.next();
					if (currentJvnServer.equals(js)) {
						if (currentJvnObjectServersCouple.getJvnObjectMemberState() == JvnObjectState.W) {
							currentJvnObjectServersCouple.setLatestJvnObjectContent(
									currentJvnObjectServersCouple.getJvnObjectMember().jvnInvalidateWriter());
						}
						currentJvnObjectServersCouple.getOwnerServers().remove(currentJvnServer);
						if (currentJvnObjectServersCouple.getOwnerServers().isEmpty()) {
							currentJvnObjectServersCouple.setJvnObjectMemberState(JvnObjectState.NL);
						}
						break;

					}
				}

			}

		}
		System.out.println("Server : \n" + js.toString() +"\n has terminated");

	}

}