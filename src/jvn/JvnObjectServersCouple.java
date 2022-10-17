package jvn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import enums.JvnObjectState;

public class JvnObjectServersCouple {
	private JvnObject jvnObjectMember;
	private List<JvnRemoteServer> ownerServers = new ArrayList<>();
	private JvnObjectState jvnObjectMemberState;
	private Serializable latestJvnObjectContent;
	
	public Serializable getLatestJvnObjectContent() {
		return latestJvnObjectContent;
	}

	public void setLatestJvnObjectContent(Serializable latestJvnObjectContent) {
		this.latestJvnObjectContent = latestJvnObjectContent;
	}

	public JvnObjectServersCouple(JvnObject jvnObjectMember, JvnRemoteServer ownerServer, JvnObjectState jvnObjectMemberState ) {
		this.jvnObjectMember= jvnObjectMember;
		this.ownerServers.add(ownerServer);
		this.jvnObjectMemberState= jvnObjectMemberState;
		
	}

	public JvnObject getJvnObjectMember() {
		return jvnObjectMember;
	}

	public void setJvnObjectMember(JvnObject jvnObjectMember) {
		this.jvnObjectMember = jvnObjectMember;
	}

	public List<JvnRemoteServer> getOwnerServers() {
		return ownerServers;
	}

	public void setOwnerServers(List<JvnRemoteServer> ownerServers) {
		this.ownerServers = ownerServers;
	}

	public JvnObjectState getJvnObjectMemberState() {
		return jvnObjectMemberState;
	}

	public void setJvnObjectMemberState(JvnObjectState jvnObjectMemberState) {
		this.jvnObjectMemberState = jvnObjectMemberState;
	}
	

}