package jvn;

import java.io.Serializable;

import enums.JvnObjectState;

public class JvnObjectImpl implements JvnObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int id;
	private JvnObjectState jvnObjectState;

	private Serializable currentJvnObject = null;

	public JvnObjectImpl() {
		super();
	}

	public JvnObjectImpl(int id, JvnObjectState jvnObjectState, Serializable currentJvnObject) {
		super();
		this.id = id;
		this.jvnObjectState = jvnObjectState;
		this.currentJvnObject = currentJvnObject;
	}

	@Override
	public synchronized void jvnLockRead() throws JvnException {
		// TODO Auto-generated method stub
		this.jvnObjectState = JvnObjectState.R;
	}

	@Override
	public synchronized void jvnLockWrite() throws JvnException {
		// TODO Auto-generated method stub

	}

	@Override
	public void jvnUnLock() throws JvnException {
		// TODO Auto-generated method stub
		switch (jvnObjectState) {
		case NL:
			this.notify();
			break;
		case R:
			this.jvnObjectState = JvnObjectState.RC;
		case W:
			this.jvnObjectState = JvnObjectState.WC;

		default:
			throw new IllegalArgumentException("Unexpected value: " + jvnObjectState);
		}
	}

	@Override
	public int jvnGetObjectId() throws JvnException {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public Serializable jvnGetSharedObject() throws JvnException {
		// TODO Auto-generated method stub
		return currentJvnObject;
	}

	@Override
	public void jvnInvalidateReader() throws JvnException {
		if (this.jvnObjectState == JvnObjectState.R) {
			while (jvnObjectState == JvnObjectState.R) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			this.jvnObjectState = JvnObjectState.NL;
		}

	}

	@Override
	public Serializable jvnInvalidateWriter() throws JvnException {
		// TODO Auto-generated method stub
		if (jvnObjectState == JvnObjectState.W) {
			while (jvnObjectState == JvnObjectState.W) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			jvnObjectState = JvnObjectState.NL;
		}
		return currentJvnObject;
	}

	@Override
	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		// TODO Auto-generated method stub
		if (jvnObjectState.equals(JvnObjectState.W)) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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
