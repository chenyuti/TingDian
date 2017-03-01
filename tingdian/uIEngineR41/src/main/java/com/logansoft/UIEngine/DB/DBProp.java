package com.logansoft.UIEngine.DB;



/**
 * @author zhangxing
 *
 */
public class DBProp {

	public String dbFileName;
	public int dbVersion;
	public int res = -1;
	public DBProp(String dbFileName, int dbVersion) {
		super();
		this.dbFileName = dbFileName;
		this.dbVersion = dbVersion;
	} 
	public DBProp(String dbFileName,int res, int dbVersion) {
	    super();
	    this.dbFileName = dbFileName;
	    this.dbVersion = dbVersion;
	    this.res = res;
	} 
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DBProp) {
			DBProp that = (DBProp) obj;
			return this.dbFileName.equals(that.dbFileName)
					&& this.dbVersion == that.dbVersion;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return dbFileName.hashCode()*dbVersion;
	}
	
}
