package eu.nanairo.orm;

import junit.framework.TestCase;

public class NanairoTemplateTest extends TestCase {
	public void testConvertColumns_01() {
		String[] columns = NanairoTemplate.convertColumns(SampleNewEntity.class);
		assertNotNull(columns[0]);
		assertEquals("ID", columns[0]);
		assertEquals("TITLE", columns[1]);
		assertEquals("NEW_OLD_TITLE", columns[2]);
	}

	public void testGetSqlForInsert_01() {
		SampleNewEntity sampleEntity = new SampleNewEntity();
		sampleEntity.setId(3L);
		sampleEntity.setNewOldTitle("abc");
		String sql = NanairoTemplate.getSqlForInsert(SampleNewEntity.class, sampleEntity);
		assertEquals("INSERT INTO SAMPLE_NEW (ID, NEW_OLD_TITLE) VALUES (?, ?)", sql);
	}

	public void testGetTableName_01() {
		String tableName = NanairoTemplate.getTableName(SampleNewEntity.class);
		assertEquals("SAMPLE_NEW", tableName);
	}

}