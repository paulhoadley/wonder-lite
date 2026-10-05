package er.extensions.foundation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.erxtest.EOFTestCase;
import er.erxtest.model.Company;
import er.extensions.eof.ERXEC;

public class ERXThreadStorageTest extends EOFTestCase {

	// public static final java.lang.String KEYS_ADDED_IN_CURRENT_THREAD_KEY;
	// public static final java.lang.String WAS_CLONED_MARKER;

	static NSMutableDictionary<String,List<Throwable>> exceptions = new NSMutableDictionary<String,List<Throwable>>();

	synchronized public static void addException(String test, Throwable t) {
		if (! exceptions.containsKey(test))
			exceptions.setObjectForKey(new ArrayList<>(), test);
		exceptions.objectForKey(test).add(t);
	}

	synchronized static boolean hasException(String test) {
		return exceptions.containsKey(test);
	}

	private static void awaitTesters(String test, Thread... testers) {
		for (Thread tester : testers) {
			try {
				tester.join(10000);
			} catch (InterruptedException e) {
				addException(test, e);
			}
			if (tester.isAlive()) {
				fail(test + " didn't finish");
			}
		}
		if (hasException(test)) {
			fail(test + " failed in a thread: " + exceptions.objectForKey(test));
		}
	}

	@Test
	public void testConstructor() {
		ExecutorService pool = Executors.newFixedThreadPool(3);
		RunnableStorageTester t1 = new RunnableStorageTester(7, "testConstructor", null, null); pool.submit(t1);
		RunnableStorageTester t2 = new RunnableStorageTester(13, "testConstructor", null, null); pool.submit(t2);
		RunnableStorageTester t3 = new RunnableStorageTester(23, "testConstructor", null, null); pool.submit(t3);
		pool.shutdown();
		boolean finished = true;
		try {
			finished = pool.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			addException("testConstructor", e);
		}
		if (hasException("testConstructor") || ! finished) fail();
	}

	private class RunnableStorageTester extends Thread {

		private int pause;
		private String test;
		//private NSArray<EOEnterpriseObject> eos;
		//private NSArray<String> keys;

		protected RunnableStorageTester(int pauseInterval,
				String testName,
				NSArray<EOEnterpriseObject> objects,
				NSArray<String> keyStrings) {
			pause = pauseInterval;
			test = testName;
			//eos = objects;
			//keys = keyStrings;
		}

		@Override
		public void run() {
			try {
				runTest();
			} catch (Throwable t) {
				addException(test, t);
			}
		}

		private void runTest() {

			Map map1 = null, map2 = null, map3 = null;

			EOEditingContext ec = null;

			Company c1 = null;

			//System.out.println("\n"+this+": "+System.currentTimeMillis()+": "+test+" start");

			try {
				sleep(pause);
			} catch (java.lang.InterruptedException ie) { System.out.println("\n"+this+": "+System.currentTimeMillis()+": "+test+" interrupted1"); }
			//System.out.println("\n"+this+": "+System.currentTimeMillis()+": "+test+" awake1");

			if ("testConstructor".equals(test)) {
				try {
					assertNotNull(new ERXThreadStorage());
				} catch (Throwable t) {
					addException("testConstructor", t);
				}
			}

			if ("testMap".equals(test)) {
				try {
					map1 = ERXThreadStorage.map();
					assertNotNull(map1);
				} catch (Throwable t) {
					addException("testMap", t);
				}
			}

			if ("testReset".equals(test)) {
				try {
					ec = ERXEC.newEditingContext();
					c1 = Company.createCompany(ec, "Acme");
					ec.saveChanges();
					ERXThreadStorage.takeValueForKey(c1, "Acme");
					assertEquals(1, ERXThreadStorage.map().size());
					ERXThreadStorage.reset();
					assertEquals(0, ERXThreadStorage.map().size());
				} catch (Throwable t) {
					addException("testReset", t);
				}
			}

                        if ("testTakeValueForKey".equals(test)) {
                                try {
                                        ec = ERXEC.newEditingContext();
                                        ERXThreadStorage.takeValueForKey(Company.createCompany(ec, "Acme"), "Acme");
					ERXThreadStorage.takeValueForKey(Company.createCompany(ec, "Bondo"), "Bondo");
					ec.saveChanges();
					ERXThreadStorage.takeValueForKey(Company.createCompany(ec, "Charlie"), "Charlie");
                                        assertEquals(((Company)ERXThreadStorage.valueForKey("Acme")).name(), "Acme");
                                        assertEquals(((Company)ERXThreadStorage.valueForKey("Bondo")).name(), "Bondo");
					assertEquals(((Company)ERXThreadStorage.valueForKey("Charlie")).name(), "Charlie");
                                } catch (Throwable t) {
                                        addException("testTakeValueForKey", t);
                                }
                        }

			if ("testRemoveValueForKey".equals(test)) {
				try {
					ec = ERXEC.newEditingContext();
					ERXThreadStorage.takeValueForKey(Company.createCompany(ec, "Acme"), "Acme");
					assertEquals(((Company)ERXThreadStorage.valueForKey("Acme")).name(), "Acme");
					ERXThreadStorage.removeValueForKey("Acme");
					assertNull(ERXThreadStorage.valueForKey("Acme"));
                                        ERXThreadStorage.takeValueForKey(Company.createCompany(ec, "Bondo"), "Acme");
					assertNotNull(ERXThreadStorage.valueForKey("Acme"));
				} catch (Throwable t) {
					addException("testRemoveValueForKey", t);
				}
			}

			try {
				sleep(pause);
			} catch (java.lang.InterruptedException ie) { System.out.println("\n"+this+": "+System.currentTimeMillis()+": "+test+" interrupted2"); }

			//System.out.println("\n"+this+": "+System.currentTimeMillis()+": "+test+" awake2");

			if ("testConstructor".equals(test)) {
				try {
					assertNotNull(new ERXThreadStorage());
				} catch (Throwable t) {
					addException("testConstructor", t);
				}
			}

			if ("testMap".equals(test)) {
				try {
					map2 = ERXThreadStorage.map();
					assertNotNull(map2);
				} catch (Throwable t) {
					addException("testMap", t);
				}
			}

			if ("testReset".equals(test)) {
				try {
					ERXThreadStorage.takeValueForKey(c1, "Acme");
					assertEquals(1, ERXThreadStorage.map().size());
					ERXThreadStorage.reset();
					assertEquals(0, ERXThreadStorage.map().size());
				} catch (Throwable t) {
					addException("testReset", t);
				}
			}

			if ("testTakeValueForKey".equals(test)) {
				assertEquals(((Company)ERXThreadStorage.valueForKey("Acme")).name(), "Acme");
				assertEquals(((Company)ERXThreadStorage.valueForKey("Bondo")).name(), "Bondo");
				assertEquals(((Company)ERXThreadStorage.valueForKey("Charlie")).name(), "Charlie");
			}

			if ("testRemoveValueForKey".equals(test)) {
				assertNotNull(ERXThreadStorage.valueForKey("Acme"));
				ERXThreadStorage.removeValueForKey("Acme");
				assertNull(ERXThreadStorage.valueForKey("Acme"));
				ERXThreadStorage.takeValueForKey(Company.createCompany(ec, "Charlie"), "Acme");
			}

			try {
				sleep(pause);
			} catch (java.lang.InterruptedException ie) { System.out.println("\n"+this+": "+System.currentTimeMillis()+": "+test+" interrupted3"); }

			//System.out.println("\n"+this+": "+System.currentTimeMillis()+": "+test+" awake3");

			if ("testConstructor".equals(test)) {
				try {
					assertNotNull(new ERXThreadStorage());
				} catch (Throwable t) {
					addException("testConstructor", t);
				}
			}

			if ("testMap".equals(test)) {
				try {
					map3 = ERXThreadStorage.map();
					assertNotNull(map3);
				} catch (Throwable t) {
					addException("testMap", t);
				}
			}

			if ("testReset".equals(test)) {
				try {
					assertEquals(0, ERXThreadStorage.map().size());
				} catch (Throwable t) {
					addException("testReset", t);
				}
			}

			if ("testTakeValueForKey".equals(test)) {
				ERXThreadStorage.takeValueForKey(Company.createCompany(ec, "Delta"), "Delta");
				assertEquals(((Company)ERXThreadStorage.valueForKey("Acme")).name(), "Acme");
				assertEquals(((Company)ERXThreadStorage.valueForKey("Bondo")).name(), "Bondo");
				assertEquals(((Company)ERXThreadStorage.valueForKey("Charlie")).name(), "Charlie");
				assertEquals(((Company)ERXThreadStorage.valueForKey("Delta")).name(), "Delta");
			}

			if ("testRemoveValueForKey".equals(test)) {
				assertNotNull(ERXThreadStorage.valueForKey("Acme"));
				ERXThreadStorage.removeValueForKey("Acme");
				assertNull(ERXThreadStorage.valueForKey("Acme"));
			}
		}
	}

	@Test
	public void testTakeValueForKey() {
		RunnableStorageTester t0 = new RunnableStorageTester(3, "testTakeValueForKey", null, null);
		RunnableStorageTester t1 = new RunnableStorageTester(7, "testTakeValueForKey", null, null);
		RunnableStorageTester t2 = new RunnableStorageTester(13, "testTakeValueForKey", null, null);
		RunnableStorageTester t3 = new RunnableStorageTester(23, "testTakeValueForKey", null, null);
		t0.start(); t1.start(); t2.start(); t3.start();
		awaitTesters("testTakeValueForKey", t0, t1, t2, t3);
	}

	@Test
	public void testRemoveValueForKey() {
		RunnableStorageTester t0 = new RunnableStorageTester(3, "testRemoveValueForKey", null, null);
		RunnableStorageTester t1 = new RunnableStorageTester(7, "testRemoveValueForKey", null, null);
		RunnableStorageTester t2 = new RunnableStorageTester(13, "testRemoveValueForKey", null, null);
		RunnableStorageTester t3 = new RunnableStorageTester(23, "testRemoveValueForKey", null, null);
		t0.start(); t1.start(); t2.start(); t3.start();
		awaitTesters("testRemoveValueForKey", t0, t1, t2, t3);
	}

	@Test
	public void testMap() {
                RunnableStorageTester t0 = new RunnableStorageTester(3, "testMap", null, null);
		RunnableStorageTester t1 = new RunnableStorageTester(7, "testMap", null, null);
		RunnableStorageTester t2 = new RunnableStorageTester(13, "testMap", null, null);
		RunnableStorageTester t3 = new RunnableStorageTester(23, "testMap", null, null);
		t0.start(); t1.start(); t2.start(); t3.start();
		awaitTesters("testMap", t0, t1, t2, t3);
	}

	@Test
	public void testReset() {
                RunnableStorageTester t0 = new RunnableStorageTester(3, "testReset", null, null);
		RunnableStorageTester t1 = new RunnableStorageTester(7, "testReset", null, null);
		RunnableStorageTester t2 = new RunnableStorageTester(13, "testReset", null, null);
		RunnableStorageTester t3 = new RunnableStorageTester(23, "testReset", null, null);
		t0.start(); t1.start(); t2.start(); t3.start();
		awaitTesters("testReset", t0, t1, t2, t3);
	}

}
