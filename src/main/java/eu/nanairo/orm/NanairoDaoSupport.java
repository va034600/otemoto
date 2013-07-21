package eu.nanairo.orm;

import android.database.sqlite.SQLiteDatabase;

public abstract class NanairoDaoSupport {
	/***/
	private NanairoTemplate nanairoTemplate = new NanairoTemplate();

	public void setDb(SQLiteDatabase db) {
		this.nanairoTemplate.setDb(db);
	}

	public NanairoTemplate getNanairoTemplate() {
		return nanairoTemplate;
	}
}
