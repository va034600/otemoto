package com.gmail.va034600.otemoto;

import junit.framework.TestCase;

public class OtemotoTemplateTest extends TestCase {
	public void testConvertColumns_01() {
		String[] columns = OtemotoTemplate.convertColumns(SampleNewEntity.class);
		assertNotNull(columns[0]);
		assertEquals("ID", columns[0]);
		assertEquals("TITLE", columns[1]);
		assertEquals("NEW_OLD_TITLE", columns[2]);
	}

	public void testGetSqlForInsert_01() {
		SampleNewEntity sampleEntity = new SampleNewEntity();
		sampleEntity.setId(3L);
		sampleEntity.setNewOldTitle("abc");
		String sql = OtemotoTemplate.getSqlForInsert(SampleNewEntity.class, sampleEntity);
		assertEquals("INSERT INTO SAMPLE_NEW (ID, NEW_OLD_TITLE) VALUES (?, ?)", sql);
	}

	public void testGetTableName_01() {
		String tableName = OtemotoTemplate.getTableName(SampleNewEntity.class);
		assertEquals("SAMPLE_NEW", tableName);
	}

}