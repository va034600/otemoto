package com.gmail.va034600.otemoto;

import android.database.sqlite.SQLiteDatabase;

public abstract class OtemotoDaoSupport {
	/***/
	private OtemotoTemplate nanairoTemplate = new OtemotoTemplate();

	public void setDb(SQLiteDatabase db) {
		this.nanairoTemplate.setDb(db);
	}

	public OtemotoTemplate getNanairoTemplate() {
		return nanairoTemplate;
	}
}
