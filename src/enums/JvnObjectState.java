package enums;

public enum JvnObjectState {
	NL, //No lock
	RC, //read lock cached
	WC, //write lock cached
	R, //Read lock taken
	W, //Write lock taken
	RWC //write lock cached and read taken

}
