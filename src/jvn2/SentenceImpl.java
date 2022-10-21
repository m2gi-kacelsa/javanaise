/***
 * Sentence class : used for keeping the text exchanged between users
 * during a chat application
 * Contact: 
 *
 * Authors: 
 */

package jvn2;

public class SentenceImpl implements java.io.Serializable, ISentence {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String 	data;
  
	public SentenceImpl() {
		data = new String("");
	}
	
	@MethodType(type = "write")
	public void write(String text) {
		data = text;
	}
	
	@MethodType(type = "read")
	public String read() {
		return data;	
	}
	
}