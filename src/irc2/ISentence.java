package irc2;

import enums.MethodType;

public interface ISentence {

	@MethodType(type = "read")
	public String read();

	@MethodType(type = "write")
	public void write(String text);
}
