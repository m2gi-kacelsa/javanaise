package jvn;

import java.io.Serializable;

import enums.JvnObjectState;

public class JvnObjectImpl implements JvnObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int id = 1;
	private JvnObjectState jvnObjectState;

	private Serializable currentJvnObject = null;

	public JvnObjectImpl() {
		super();
	}

	public JvnObjectImpl( Serializable currentJvnObject) {
		super();
		this.jvnObjectState = JvnObjectState.W;
		this.currentJvnObject = currentJvnObject;
	}

	@Override
	public void jvnLockRead() throws JvnException {
		// TODO Auto-generated method stub
		synchronized (this) {
			if (jvnObjectState == JvnObjectState.RC) {
				this.jvnObjectState = JvnObjectState.R;
			} else if (jvnObjectState == JvnObjectState.R){
				System.out.println("Le Lock est déja en lecture!!");			}
		}

	}

	@Override
	public synchronized void jvnLockWrite() throws JvnException {
		// TODO Auto-generated method stub

		synchronized (this) {
			if (jvnObjectState == JvnObjectState.WC) {
				jvnObjectState = JvnObjectState.W;
			} else if (jvnObjectState == JvnObjectState.W){
				System.out.println("Le Lock est déja en écriture!!");
			}
		}

	}

	@Override
	public void jvnUnLock() throws JvnException {
		// TODO Auto-generated method stub
		synchronized (this) {
			switch (jvnObjectState) {
			case NL:
				this.notifyAll();
				break;
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
		return id;
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
