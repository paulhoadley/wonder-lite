package er.erxtest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOObjectStore;
import com.wounit.rules.TemporaryEditingContext;

import er.extensions.eof.ERXEC;
import er.extensions.foundation.ERXProperties;

/**
 * Base class for the EOF tests salvaged from Wonder's ERXTest application. They run
 * against the ERXTest model on the Memory adaptor, through WOUnit's
 * {@link TemporaryEditingContext}, which empties the store after every test. The model
 * itself is loaded once, as in an app.
 * <p>
 * WOUnit makes {@link ERXEC#newEditingContext()} return its own editing context. These
 * tests exercise {@code ERXEC} itself, so instead each one gets a new editing context
 * each time it asks, as in an app, and they're all disposed of after the test, as at the
 * end of a request. WOUnit's editing context stays unlocked during the test, since
 * nothing uses it.
 *
 * @since 0.3
 */
public abstract class EOFTestCase {
	/**
	 * The name of the model the tests use.
	 */
	public static final String MODEL_NAME = "ERXTest";

	static {
		// What ERXTest's Properties set, and the tests expect
		ERXProperties.setStringForKey("true", "er.extensions.ERXEC.defaultAutomaticLockUnlock");
		ERXProperties.setStringForKey("false", "er.extensions.ERXEC.defaultCoalesceAutoLocks");
		ERXProperties.setStringForKey("true", "er.extensions.ERXEC.useUnlocker");
		ERXProperties.setStringForKey("false", "er.extensions.ERXEC.traceOpenLocks");
		ERXProperties.setStringForKey("false", "er.extensions.ERXEC.useSharedEditingContext");
		ERXProperties.setStringForKey("true", "er.extensions.ERXEnterpriseObject.applyRestrictingQualifierOnInsert");
		// For ERXModelGroupTest, which checks that this chooses the default model group's class
		ERXProperties.setStringForKey("er.extensions.eof.SortOfModelGroup", "er.extensions.defaultModelGroupClassName");
		// Loads the model once, as an app does. WOUnit unloads only the models it loaded for
		// a test, so this one stays. Reloaded for each test, its entities would be new
		// objects, but ERXEntityClassDescription registers a model's class descriptions only
		// the first time it sees the model's name, and EOs would keep the first entities.
		new TemporaryEditingContext(MODEL_NAME);
	}

	/**
	 * Empties the store after each test.
	 */
	@RegisterExtension
	protected final TemporaryEditingContext temporaryEditingContext = new TemporaryEditingContext(MODEL_NAME);

	/**
	 * The editing contexts {@code ERXEC} has made during this test.
	 */
	private final List<EOEditingContext> editingContexts = Collections.synchronizedList(new ArrayList<>());

	/**
	 * Replaces WOUnit's {@code ERXEC} factory with one that makes a new editing context each
	 * time and keeps track of it, and unlocks WOUnit's editing context. Left locked, that
	 * would be unlocked by any test that ends a request, through {@code ERXEC}'s unlocker,
	 * and WOUnit's own unlock would then fail.
	 */
	@BeforeEach
	public void setUpEditingContexts() {
		ERXEC.setFactory(new ERXEC.DefaultFactory() {
			@Override
			protected EOEditingContext _createEditingContext(EOObjectStore parent) {
				EOEditingContext editingContext = super._createEditingContext(parent);
				editingContexts.add(editingContext);
				return editingContext;
			}
		});
		temporaryEditingContext.unlock();
	}

	/**
	 * Disposes of the test's editing contexts, newest first, so that nested ones go before
	 * their parents, and locks WOUnit's editing context again, for WOUnit to unlock.
	 * <p>
	 * The store starts again after each test, and so do its primary keys, so later tests
	 * reuse global IDs. An editing context left for the garbage collector would release its
	 * snapshots when it's finalized, including those that a later test's objects with the
	 * same global IDs depend on. A test that makes an editing context some other way, and
	 * saves through it, should dispose of it too.
	 */
	@AfterEach
	public void tearDownEditingContexts() {
		try {
			synchronized (editingContexts) {
				for (int i = editingContexts.size() - 1; i >= 0; i--) {
					dispose(editingContexts.get(i));
				}
			}
		}
		finally {
			temporaryEditingContext.lock();
		}
	}

	/**
	 * Disposes of an editing context, unless the test already has, or another thread has it
	 * locked. Disposing of it twice fails, and waiting for that thread could take forever.
	 */
	private static void dispose(EOEditingContext editingContext) {
		if (wasDisposed(editingContext) || !editingContext.tryLock()) {
			return;
		}
		editingContext.unlock();
		editingContext.dispose();
	}

	private static boolean wasDisposed(EOObjectStore objectStore) {
		try {
			Field wasDisposed = EOObjectStore.class.getDeclaredField("_wasDisposed");
			wasDisposed.setAccessible(true);
			return wasDisposed.getBoolean(objectStore);
		}
		catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Returns the name of the adaptor the model uses, which WOUnit sets to "Memory".
	 *
	 * @return the adaptor name
	 */
	public static String adaptorName() {
		return EOModelGroup.defaultGroup().modelNamed(MODEL_NAME).adaptorName();
	}
}
