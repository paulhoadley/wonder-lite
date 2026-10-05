package er.plugintest.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXBatchFetchUtilities;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;
import er.plugintest.model.City;
import er.plugintest.model.Country;
import er.plugintest.model.CountryLanguage;

public class ReadTest extends PluginTestCase {

	public static final Logger log = Logger.getLogger(ReadTest.class);

	@BeforeEach
	public void setUp() throws Exception {
		resetData();
	}

	@Test
	public void testFetchAll() {
		ERXEC ec = (ERXEC) ERXEC.newEditingContext();
		ec.lock();
		try {
			
			NSArray<City> cities = City.fetchAllCities(ec);
			
			assertNotNull(cities);
			assertEquals(28, cities.count());
		} finally {
			ec.unlock();
		}
	}
	
	@Test
	public void testSimpleQual() {
		ERXEC ec = (ERXEC) ERXEC.newEditingContext();
		ec.lock();
		try {
			
			NSArray<City> cities = City.fetchCities(ec, City.NAME.like("Rotter*"), null);
			
			assertNotNull(cities);
			assertEquals(1, cities.count());
		} finally {
			ec.unlock();
		}
	}

	@Test
	public void testCaseInsensativeQual() {
		ERXEC ec = (ERXEC) ERXEC.newEditingContext();
		ec.lock();
		try {
			
			NSArray<City> cities = City.fetchCities(ec, City.NAME.likeInsensitive("roTTer*"), null);
			
			assertNotNull(cities);
			assertEquals(1, cities.count());
		} finally {
			ec.unlock();
		}
	}
	
	@Test
	public void testJoin() {
		ERXEC ec = (ERXEC) ERXEC.newEditingContext();
		ec.lock();
		try {
			
			NSArray<City> cities = City.fetchCities(ec, City.COUNTRY.dot(Country.CODE).eq("NLD"), null);
			
			assertNotNull(cities);
			assertEquals(28, cities.count());
		} finally {
			ec.unlock();
		}
	}
	
	@Test
	public void testLeftJoin() {
		ERXEC ec = (ERXEC) ERXEC.newEditingContext();
		ec.lock();
		try {
			
			NSArray<Country> countries = Country.fetchCountries(ec, 
					Country.COUNTRY_LANGUAGES.dot(CountryLanguage.LANGUAGE).eq("Dutch").or(  // this is left join
							Country.NAME.like("Nor*"))
					, Country.NAME.ascs());
			
			assertNotNull(countries);
			assertEquals(2, countries.count());
		} finally {
			ec.unlock();
		}
		
	}

	@Test
	public void testSortOrder() {
		ERXEC ec = (ERXEC) ERXEC.newEditingContext();
		ec.lock();
		try {
			
			NSArray<City> cities = City.fetchCities(ec, City.NAME.like("A*"),  City.NAME.ascInsensitives());
			
			assertNotNull(cities);
			City city = cities.objectAtIndex(0);
			assertNotNull(city);
			log.debug(city.name());
			assertEquals("Alkmaar", city.name());
		} finally {
			ec.unlock();
		}
	}

	@Test
	public void testBatchFetch() {
		ERXEC ec = (ERXEC) ERXEC.newEditingContext();
		ec.lock();
		try {
			
			NSArray<Country> countries = Country.fetchAllCountries(ec);
			
			assertNotNull(countries);
			assertEquals(2, countries.count());
			ERXBatchFetchUtilities.batchFetch(countries, Country.CITIES_KEY);
		} finally {
			ec.unlock();
		}
	}
	
	@Test
	public void testLimitedFetch() {
		ERXEC ec = (ERXEC) ERXEC.newEditingContext();
		ec.lock();
		try {
			
			EOFetchSpecification fs = new EOFetchSpecification(City.ENTITY_NAME, null, City.NAME.ascs());
			NSArray<City> cities = ERXEOControlUtilities.objectsInRange(ec, fs, 1, 5);
			
			assertNotNull(cities);
			assertEquals(4, cities.count());
		} finally {
			ec.unlock();
		}
	}

	
	@Test
	public void testMoreComplexLimitedFetch() {
		ERXEC ec = (ERXEC) ERXEC.newEditingContext();
		ec.lock();
		try {
			
			EOFetchSpecification fs = new EOFetchSpecification(City.ENTITY_NAME, City.COUNTRY.dot(Country.CODE).eq("NLD"), City.NAME.ascs());
			NSArray<City> cities = ERXEOControlUtilities.objectsInRange(ec, fs, 1, 5);
			
			assertNotNull(cities);
			assertEquals(4, cities.count());
		} finally {
			ec.unlock();
		}
	}
	
	
//
//	public void testFetchAll() {
//		ERXEC ec = (ERXEC) ERXEC.newEditingContext();
//		ec.lock();
//		try {
//			
//			NSArray<City> cities = City.fetchAllCities(ec);
//			
//			assertNotNull(cities);
//			assertEquals(29, cities.count());
//		} finally {
//			ec.unlock();
//		}
//	}
//
//	public void testFetchAll() {
//		ERXEC ec = (ERXEC) ERXEC.newEditingContext();
//		ec.lock();
//		try {
//			
//			NSArray<City> cities = City.fetchAllCities(ec);
//			
//			assertNotNull(cities);
//			assertEquals(29, cities.count());
//		} finally {
//			ec.unlock();
//		}
//	}
//
//	public void testFetchAll() {
//		ERXEC ec = (ERXEC) ERXEC.newEditingContext();
//		ec.lock();
//		try {
//			
//			NSArray<City> cities = City.fetchAllCities(ec);
//			
//			assertNotNull(cities);
//			assertEquals(29, cities.count());
//		} finally {
//			ec.unlock();
//		}
//	}

}
