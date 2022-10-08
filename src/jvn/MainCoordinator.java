package jvn;

public class MainCoordinator {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try {
			//create reference of coordinator in rmi registry,by calling constructor
			JvnRemoteCoord coord = new JvnCoordImpl();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
