package jvn;

import java.io.Serializable;

import enums.JvnObjectState;

public class JvnObjectImpl implements JvnObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int jvnObjectId = 1;
	private transient JvnObjectState jvnObjectState = JvnObjectState.NL;

	private Serializable currentJvnObject = null;

	public JvnObjectImpl() {
		super();
	}

	public JvnObjectImpl(Serializable currentJvnObject) {
		super();
		this.jvnObjectState = JvnObjectState.W;
		this.currentJvnObject = currentJvnObject;
	}

	@Override
	public synchronized void jvnLockRead() throws JvnException {
		System.out.println("demande de verrou en lecture . . .");
		if (jvnObjectState == JvnObjectState.RC)
			this.jvnObjectState = JvnObjectState.R;
		else if (jvnObjectState == JvnObjectState.WC) {
			this.jvnObjectState = JvnObjectState.RWC;
		} else {
			currentJvnObject = JvnServerImpl.jvnGetServer().jvnLockRead(jvnObjectId);
			this.jvnObjectState = JvnObjectState.R;
		}
	}

	@Override
	public synchronized void jvnLockWrite() throws JvnException {
		System.out.println("demande de verrou en ecriture . . .");
		if (jvnObjectState == JvnObjectState.WC) {
			jvnObjectState = JvnObjectState.W;
		} else {
			currentJvnObject = JvnServerImpl.jvnGetServer().jvnLockWrite(jvnObjectId);
			jvnObjectState = JvnObjectState.W;
		}
	}

	@Override
	public synchronized void jvnUnLock() throws JvnException {
		// TODO Auto-generated method stub
		switch (jvnObjectState) {
		case R:
			this.jvnObjectState = JvnObjectState.RC;
			break;
		case W:
			this.jvnObjectState = JvnObjectState.WC;
			break;
		case RWC:
			this.jvnObjectState = JvnObjectState.WC;
			break;

		default:
			throw new IllegalArgumentException("Unexpected value: " + jvnObjectState);
		}
		this.notifyAll();
	}

	@Override
	public int jvnGetObjectId() throws JvnException {
		// TODO Auto-generated method stub
		return jvnObjectId;
	}

	@Override
	public Serializable jvnGetSharedObject() throws JvnException {
		// TODO Auto-generated method stub
		return currentJvnObject;
	}

	@Override
	public void jvnInvalidateReader() throws JvnException {
		if (this.jvnObjectState == JvnObjectState.R || jvnObjectState == JvnObjectState.RC
				|| jvnObjectState == JvnObjectState.RWC) {
			jvnObjectState = JvnObjectState.NL;
		}
	}

	@Override
	public synchronized Serializable jvnInvalidateWriter() throws JvnException {

			if (jvnObjectState == JvnObjectState.W) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			jvnObjectState = JvnObjectState.NL;
		} else if (jvnObjectState == JvnObjectState.WC) {
			jvnObjectState = JvnObjectState.NL;
		}
		return currentJvnObject;
	}

	@Override
	public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
		if (jvnObjectState == JvnObjectState.W) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			jvnObjectState = JvnObjectState.RC;
		} else if (jvnObjectState == JvnObjectState.WC || jvnObjectState == JvnObjectState.RWC) {
			jvnObjectState = JvnObjectState.RC;
		}
		return currentJvnObject;
	}

	public int getJvnObjectId() {
		return jvnObjectId;
	}

	public void setJvnObjectId(int jvnObjectId) {
		this.jvnObjectId = jvnObjectId;
	}

	public JvnObjectState getJvnObjectState() {
		return jvnObjectState;
	}

	public void setJvnObjectState(JvnObjectState jvnObjectState) {
		this.jvnObjectState = jvnObjectState;
	}

	public Serializable getCurrentJvnObject() {
		return currentJvnObject;
	}

	public void setCurrentJvnObject(Serializable currentJvnObject) {
		this.currentJvnObject = currentJvnObject;
	}

}
