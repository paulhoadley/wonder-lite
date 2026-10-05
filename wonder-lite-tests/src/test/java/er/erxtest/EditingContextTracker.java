package er.erxtest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOObjectStore;

import er.extensions.eof.ERXEC;

/**
 * Makes {@link ERXEC#newEditingContext()} return a new editing context each time, as in an
 * app, and keeps track of them, so that a test can dispose of them all when it finishes, as
 * at the end of a request.
 * <p>
 * The tests reuse global IDs: a store that starts again for each test starts its primary
 * keys again too, and test data inserted with SQL has fixed keys. An editing context left
 * for the garbage collector would release its snapshots when it's finalized, including
 * those that a later test's objects with the same global IDs depend on. A test that makes
 * an editing context some other way, and saves through it, should dispose of it too.
 *
 * @since 0.3
 */
public class EditingContextTracker {
	/**
	 * The editing contexts {@code ERXEC} has made since {@link #install()}.
	 */
	private final List<EOEditingContext> editingContexts = Collections.synchronizedList(new ArrayList<>());

	/**
	 * Installs an {@code ERXEC} factory that makes a new editing context each time and keeps
	 * track of it.
	 */
	public void install() {
		ERXEC.setFactory(new ERXEC.DefaultFactory() {
			@Override
			protected EOEditingContext _createEditingContext(EOObjectStore parent) {
				EOEditingContext editingContext = super._createEditingContext(parent);
				editingContexts.add(editingContext);
				return editingContext;
			}
		});
	}

	/**
	 * Disposes of the editing contexts made so far, newest first, so that nested ones go
	 * before their parents.
	 */
	public void disposeAll() {
		synchronized (editingContexts) {
			for (int i = editingContexts.size() - 1; i >= 0; i--) {
				dispose(editingContexts.get(i));
			}
			editingContexts.clear();
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
}
