
package er.extensions.eof;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.webobjects.eoaccess.EOModelGroup;

import er.erxtest.EOFTestCase;

public class ERXModelGroupTest extends EOFTestCase {

    @Test
    public void testConstructor() {
        assertNotNull(new ERXModelGroup());
    }

    @Test
    public void testSettingEOModelGroupClass() {
        assertEquals("er.extensions.eof.SortOfModelGroup", EOModelGroup.defaultGroup().getClass().getName());
    }
}
