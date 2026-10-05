package er.erxtest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.webobjects.eoaccess.EOModelGroup;
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
 * tests exercise {@code ERXEC} itself, so instead an {@link EditingContextTracker} hands
 * out a new editing context each time, as in an app. WOUnit's editing context stays
 * unlocked during the test, since nothing uses it.
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
	 * Hands out and disposes of the test's editing contexts.
	 */
	private final EditingContextTracker editingContexts = new EditingContextTracker();

	/**
	 * Replaces WOUnit's {@code ERXEC} factory with one that makes a new editing context each
	 * time, and unlocks WOUnit's editing context. Left locked, that would be unlocked by any
	 * test that ends a request, through {@code ERXEC}'s unlocker, and WOUnit's own unlock
	 * would then fail.
	 */
	@BeforeEach
	public void setUpEditingContexts() {
		editingContexts.install();
		temporaryEditingContext.unlock();
	}

	/**
	 * Disposes of the test's editing contexts, and locks WOUnit's editing context again, for
	 * WOUnit to unlock.
	 */
	@AfterEach
	public void tearDownEditingContexts() {
		try {
			editingContexts.disposeAll();
		}
		finally {
			temporaryEditingContext.lock();
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
