package er.plugintest.tests;

import org.junit.jupiter.api.Test;

public class DDLTest extends PluginTestCase {

	@Test
	public void testCreateSchema() {
		createSchema(model, adaptorName);
	}

}
