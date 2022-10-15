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
	public void jvnLockRead() throws JvnException {
		// TODO Auto-generated method stub
		synchronized (this) {
			if (jvnObjectState == JvnObjectState.RC)
				this.jvnObjectState = JvnObjectState.R;
			else {
				Serializable temp = JvnServerImpl.jvnGetServer().jvnLockRead(jvnObjectId);
				this.jvnObjectState = JvnObjectState.R;
			}

		}
	}

	@Override
	public void jvnLockWrite() throws JvnException {
		// TODO Auto-generated method stub
		synchronized (this) {
			if (jvnObjectState == JvnObjectState.WC) {
				jvnObjectState = JvnObjectState.W;
			} else {
				Serializable temp = JvnServerImpl.jvnGetServer().jvnLockRead(jvnObjectId);	
				jvnObjectState = JvnObjectState.W;
			}

		}

	}

	@Override
	public void jvnUnLock() throws JvnException {
		// TODO Auto-generated method stub
		synchronized (this) {
			switch (jvnObjectState) {
			case R:
				this.jvnObjectState = JvnObjectState.RC;
				this.notifyAll();
			case W:
				this.jvnObjectState = JvnObjectState.WC;
				this.notifyAll();

			default:
				throw new IllegalArgumentException("Unexpected value: " + jvnObjectState);
			}
		}

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
		synchronized (this) {
			if (this.jvnObjectState == JvnObjectState.R) {
				while (jvnObjectState == JvnObjectState.R) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				jvnObjectState = JvnObjectState.NL;
			} else if (jvnObjectState == JvnObjectState.RC) {
				this.jvnObjectState = JvnObjectState.NL;
			}
		}
	}

	@Override
	public Serializable jvnInvalidateWriter() throws JvnException {
		// TODO Auto-generated method stub
		synchronized (this) {
			if (jvnObjectState == JvnObjectState.W) {
				while (jvnObjectState == JvnObjectState.W) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				jvnObjectState = JvnObjectState.NL;
			} else if (jvnObjectState == JvnObjectState.WC) {
				jvnObjectState = JvnObjectState.NL;
			}
			return currentJvnObject;
		}
	}

	@Override
	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		// TODO Auto-generated method stub
		synchronized (this) {
			if (jvnObjectState == JvnObjectState.W) {
				while (jvnObjectState == JvnObjectState.W) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				jvnObjectState = JvnObjectState.RC;
			} else if (jvnObjectState == JvnObjectState.WC) {
				jvnObjectState = JvnObjectState.RC;
			}
			return currentJvnObject;
		}

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
