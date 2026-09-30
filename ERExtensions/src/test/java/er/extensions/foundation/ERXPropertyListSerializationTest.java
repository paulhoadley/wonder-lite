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

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

/**
 * Unit tests on {@link ERXPropertyListSerialization}.
 * 
 * @author paulh
 * @since 0.1
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

	// From Wonder's ERXTest (author jw)
	@Test
	public void jsonStringFromPropertyListRoundTrips() {
		// Non-ASCII characters are written unescaped
		String stringObject = "fran\u00e7ais";
		String jsonString = ERXPropertyListSerialization.jsonStringFromPropertyList(stringObject);
		assertEquals("\"fran\u00e7ais\"", jsonString);
		assertEquals(stringObject, ERXPropertyListSerialization.propertyListFromJSONString(jsonString));
		// Integer array
		NSArray<Integer> integerArray = new NSArray<>(new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) });
		assertEquals("[1,2,3]", ERXPropertyListSerialization.jsonStringFromPropertyList(integerArray));
		assertEquals("[\n\t1,\n\t2,\n\t3\n]", ERXPropertyListSerialization.jsonStringFromPropertyList(integerArray, false));
		// Dictionary
		NSDictionary<String, Integer> integerDict = new NSDictionary<>(new Integer[] { Integer.valueOf(1), Integer.valueOf(2) }, new String[] { "a", "b" });
		assertEquals("{\"a\" : 1,\"b\" : 2}", ERXPropertyListSerialization.jsonStringFromPropertyList(integerDict));
		assertEquals("{\n\t\"a\" : 1,\n\t\"b\" : 2\n}", ERXPropertyListSerialization.jsonStringFromPropertyList(integerDict, false));
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
