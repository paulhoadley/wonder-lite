package er.plugintest.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.jdbcadaptor.JDBCAdaptor;

import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXModelGroup;

/**
 * Checks ERPrototypes' prototypes for the database against the types its JDBC adaptor
 * knows. EOF can't generate SQL for a column whose external type isn't among them.
 *
 * @since 0.3
 */
public class PrototypesTest extends PluginTestCase {
	@SuppressWarnings("unchecked")
	@Test
	public void testExternalTypesAreKnown() {
		String prototypesName = ERXModelGroup.prototypeEntityNameForModel(model);
		EOEntity prototypes = EOModelGroup.defaultGroup().entityNamed(prototypesName);
		assertNotNull(prototypes, prototypesName);
		JDBCAdaptor adaptor = (JDBCAdaptor) EOUtilities.databaseContextForModelNamed(ERXEC.newEditingContext(), MODEL_NAME).adaptorContext().adaptor();
		NSDictionary<String, Object> typeInfo = (NSDictionary<String, Object>) adaptor.plugIn().jdbcInfo().objectForKey(JDBCAdaptor.TypeInfoKey);
		List<String> unknown = new ArrayList<>();
		for (EOAttribute prototype : prototypes.attributes()) {
			String externalType = prototype.externalType();
			if (typeInfo.objectForKey(externalType) == null && typeInfo.objectForKey(externalType.toUpperCase()) == null) {
				unknown.add(prototype.name() + " (" + externalType + ")");
			}
		}
		assertTrue(unknown.isEmpty(), prototypesName + " has external types the database doesn't know: " + unknown);
	}
}
