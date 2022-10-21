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
	//List of JvnObjects with their names
	private HashMap<String, JvnObject> jvnObjectsNameMap = new HashMap<>();
	//List of servers holding JVNObject with write/read state
	private HashMap<Integer, ServersAndJvnObjectWithState> listServersWithJvnObj = new HashMap<>();


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
			throw new JvnException("name of jvnobject is null!!");
		} else if (jo == null) {
			throw new JvnException("jvnObject is null");
		} else if (js == null) {
			throw new JvnException("jvnServer passed is null");
		} else if (jvnObjectsNameMap.containsKey(jon)) {
			throw new JvnException("jvnObject with such a name is already registered !");
		} else if (listServersWithJvnObj.containsKey(jo.jvnGetObjectId())) {
			throw new JvnException("Object with such an id is already registered !");
		}

		//ajouter le jvn object avec le serveur Js avec statut de l objet write
		listServersWithJvnObj.put(jo.jvnGetObjectId(), new ServersAndJvnObjectWithState(jo, js, JvnObjectState.W));
		// associer un nom au jvnobject et ajouter dans la liste
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
			throw new JvnException("jvn name is null");
		} else if (js == null) {
			throw new JvnException("the instance if server is null");
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
			ServersAndJvnObjectWithState jvnObjectAndServer = listServersWithJvnObj.get(joi);
			if (jvnObjectAndServer == null) {
				throw new JvnException(" Not found jvnObject with such an Id");
			} else if (jvnObjectAndServer.getJvnObjectMemberState() == JvnObjectState.NL) {
				jvnObjectAndServer.setJvnObjectMemberState(JvnObjectState.R);
				jvnObjectAndServer.getOwnerServers().add(js);
			} else if (jvnObjectAndServer.getJvnObjectMemberState() == JvnObjectState.R) {
				jvnObjectAndServer.getOwnerServers().add(js);

			} else if (jvnObjectAndServer.getJvnObjectMemberState() == JvnObjectState.W) {
				JvnRemoteServer ownerServer = jvnObjectAndServer.getOwnerServers().iterator().next();
				jvnObjectAndServer.setLatestJvnObjectContent(ownerServer.jvnInvalidateWriterForReader(joi));
				jvnObjectAndServer.setJvnObjectMemberState(JvnObjectState.R);
			}
			if (!jvnObjectAndServer.getOwnerServers().contains(js)) {
				jvnObjectAndServer.getOwnerServers().add(js);

			} else {
				throw new JvnException(
						"Unexpected lock state: " + jvnObjectAndServer.getJvnObjectMemberState() + " !");
			}
			System.out.println("Verrou en lecture attribué!!");
			return jvnObjectAndServer.getLatestJvnObjectContent();

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
			ServersAndJvnObjectWithState jvnObjectAndServer = listServersWithJvnObj.get(joi);
			if (jvnObjectAndServer == null) {
				throw new JvnException(" Not found jvnObject with such an Id !\n");
			} else if (jvnObjectAndServer.getJvnObjectMemberState() == JvnObjectState.NL) {
				// I could,'t figure out if there is something to do in this case
			} else if (jvnObjectAndServer.getJvnObjectMemberState() == JvnObjectState.R) {
				Iterator<JvnRemoteServer> ownerServersIterator = jvnObjectAndServer.getOwnerServers()
						.iterator();
				while (ownerServersIterator.hasNext()) {
					try {
						ownerServersIterator.next().jvnInvalidateReader(joi);

					} catch (Exception e) {
						throw new JvnException("Error invalidating a read lock!\n" + e);
					}

				}
			} else if (jvnObjectAndServer.getJvnObjectMemberState() == JvnObjectState.W) {
				try {
					Iterator<JvnRemoteServer> ownerServersIterator = jvnObjectAndServer.getOwnerServers()
							.iterator();
					jvnObjectAndServer
							.setLatestJvnObjectContent(ownerServersIterator.next().jvnInvalidateWriter(joi));

				} catch (Exception e) {
					throw new JvnException("Error invalidating a write lock!\n" + e);
				}
			}

			jvnObjectAndServer.getOwnerServers().clear();
			jvnObjectAndServer.getOwnerServers().add(js);
			jvnObjectAndServer.setJvnObjectMemberState(JvnObjectState.W);
			System.out.println("Verrou en ecriture attribué!!");
			return jvnObjectAndServer.getLatestJvnObjectContent();

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
			Iterator<ServersAndJvnObjectWithState> allJvnObjectServersIterator = listServersWithJvnObj.values().iterator();
			Iterator<JvnRemoteServer> OwnerServersIterator;
			ServersAndJvnObjectWithState currentJvnObjectServersCouple;
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
		System.out.println("SERVER  HAS TERMINATED!");

	}

}