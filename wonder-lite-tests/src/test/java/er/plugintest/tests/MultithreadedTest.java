package er.plugintest.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXEC;
import er.plugintest.model.City;
import er.plugintest.model.Country;

public class MultithreadedTest extends PluginTestCase {

	
	@BeforeEach
	public void setUp() throws Exception {
		resetData();

	}
	
	
	
	
	@Test
	public void testMutipleThreads() {
		
		ArrayList<QueryTask> qTasks = new  ArrayList<MultithreadedTest.QueryTask>();
		ArrayList<Thread> qThreads = new ArrayList<Thread>();
		ArrayList<Thread> threads = new ArrayList<Thread>();
		List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());
		
		for (int i = 0; i < 25; i++) {
			QueryTask task = new QueryTask();
			qTasks.add(task);
			Thread thread = new Thread(task, "QueryThread-" +i);
			thread.setUncaughtExceptionHandler((t, e) -> failures.add(e));
			qThreads.add(thread);
			thread.start();
		}
		
		for (int i = 0; i < 5; i++) {
			InsertTask task = new InsertTask();
			threads.add(new Thread(task, "InsertThread-" + i));
		}
		
		for (int i = 0; i < 5; i++) {
			UpdateTask task = new UpdateTask();
			threads.add(new Thread(task, "UpdateThread-" + i));
		}
		
		for (Thread thread : threads) {
			thread.setUncaughtExceptionHandler((t, e) -> failures.add(e));
			thread.start();
		}
		
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
			}
		}
		
		for (QueryTask task : qTasks) {
			task.setRun(false);
		}

		for (Thread thread : qThreads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
			}
		}

		assertTrue(failures.isEmpty(), "Failed in a thread: " + failures);
	}
	
	
	public static class QueryTask implements Runnable {

		private boolean run = true;
		@Override
		public void run() {
			
			while (getRun()) {
				ERXEC ec = (ERXEC) ERXEC.newEditingContext();
				ec.lock();
				try {
					
					NSArray<City> cities = City.fetchCities(ec, City.NAME.likeInsensitive(RandomStringUtils.randomAlphabetic(1) + "*"), City.NAME.ascs());
					
				} finally {
					ec.unlock();
					ec.dispose();
				}
			}
		}
		public synchronized void setRun(boolean run) {
			this.run = run;
		}
		public  synchronized boolean getRun() {
			return run;
		}
		
	}

	public static class InsertTask implements Runnable {

		@Override
		public void run() {
			for (int i = 0; i < 25; i++) {
				ERXEC ec = (ERXEC) ERXEC.newEditingContext();
				ec.lock();
				try {
					Country country = Country.fetchAllCountries(ec).lastObject();
					City city = City.createCity(ec, RandomStringUtils.randomAlphabetic(15));
					city.setCountryRelationship(country);
					
					ec.saveChanges();
				} finally {
					ec.unlock();
					ec.dispose();
				}
				
			}
		}
		
	}

	public static class UpdateTask implements Runnable {

		@Override
		public void run() {
			for (int i = 0; i < 25; i++) {
				ERXEC ec = (ERXEC) ERXEC.newEditingContext();
				ec.lock();
				try {
					City city = City.fetchCity(ec, City.NAME.eq("Amsterdam"));
					
					if (city != null) {
						city.setPopulation(RandomUtils.nextInt(0, 3));
					}
					
					ec.saveChanges();
				} finally {
					ec.unlock();
					ec.dispose();
				}
				
			}
			
		}
		
	}
}
