package er.extensions.foundation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

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

	@Test
	public void convertDOMToStringReturnsExpectedResult() {
		Document document = createDocumentFromResource(SAMPLE_XML_PLIST);
		String result = ERXPropertyListSerialization.convertDOMToString(document);
		String expected = stringForResource(EXPECTED_XML_PLIST);
		assertEquals(expected, result);
	}

	protected static Document createDocumentFromResource(String resource) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		try (InputStream is = ERXPropertyListSerialization.class.getResourceAsStream(resource)) {
			DocumentBuilder db = dbf.newDocumentBuilder();
			return db.parse(is);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String stringForResource(String resource) {
		return new BufferedReader(new InputStreamReader(ERXPropertyListSerialization.class.getResourceAsStream(resource), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
	}
}
