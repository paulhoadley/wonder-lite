package er.extensions.foundation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests on {@link ERXPropertyListSerialization}.
 * 
 * @author paulh
 * @since wonder-lite-1.0
 */
public class ERXPropertyListSerializationTest {
	private static final String SAMPLE_XML_PLIST = "/sample-xml.plist";
	private static final String EXPECTED_XML_PLIST = "/expected-xml.plist";

	@Test
	public void convertDOMToStringReturnsNullForNull() {
		assertNull(ERXPropertyListSerialization.convertDOMToString(null));
		return;
	}

	// This really only tests round-tripping from XML-format property list and back
	@Test
	public void convertDOMToStringReturnsExpectedResult() {
		Document document = documentFromResource(SAMPLE_XML_PLIST);
		String result = ERXPropertyListSerialization.convertDOMToString(document);
		String expected = stringFromResource(EXPECTED_XML_PLIST);
		assertEquals(expected, result);
		return;
	}

	private static Document documentFromResource(String resource) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		try (InputStream is = ERXPropertyListSerialization.class.getResourceAsStream(resource)) {
			DocumentBuilder db = dbf.newDocumentBuilder();
			return db.parse(is);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String stringFromResource(String resource) {
		try (InputStream is = ERXPropertyListSerialization.class.getResourceAsStream(resource)) {
			return new String(is.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			return null;
		}
	}
}
