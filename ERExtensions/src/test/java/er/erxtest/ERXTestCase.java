package er.erxtest;

import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

/**
 * Base class for tests salvaged from Wonder's ERXTest. Unlike the original, it doesn't boot an
 * application, so tests that need EOF models belong elsewhere (#20).
 */
public abstract class ERXTestCase extends TestCase {
	public ERXTestCase() {
		super();
	}

	public ERXTestCase(String name) {
		super(name);
	}

	public static void assertEquals(Object[] arg0, Object[] arg1) {
		assertTrue(Arrays.toString(arg0) + " != " + Arrays.toString(arg1), Arrays.equals(arg0, arg1));
	}

	public static void assertEquals(Collection<?> arg0, Collection<?> arg1) {
		if (arg0 == null && arg1 == null)
			return;
		if ((arg0 != null && arg0.equals(arg1)) || (arg1 != null && arg1.equals(arg0)))
			return;
		TestCase.assertEquals(arg0, arg1);
	}
}
