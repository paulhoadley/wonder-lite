package er.plugintest.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.webobjects.foundation.NSData;

import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXModelGroup;
import er.plugintest.model.City;
import er.plugintest.model.Country;

public class CUDTest extends PluginTestCase {

	@BeforeEach
	public void setUp() throws Exception {
		resetData();
	}

	@Test
	public void testInsert() {
		ERXEC ec = (ERXEC) ERXEC.newEditingContext();
		ec.lock();
		try {
			City city = City.createCity(ec, "Washington");
			city.setDistict("DC");
			city.setPopulation(650000);
			city.setDescription("This is some text to make sure long strings work");

			log.debug(ERXModelGroup.prototypeEntityNameForModel(model));
			//Employee emp = Employee.createEmployee(ec, "Bugs", "Bunny", false, Company.createCompany(ec, "Boston Opera"));

			ec.saveChanges();

			Integer cnt = ERXEOControlUtilities.objectCountWithQualifier(ec, City.ENTITY_NAME, City.DISTICT.eq("DC"));

			assertNotNull(cnt);
			assertEquals(Integer.valueOf(1), cnt);

			Country country = Country.createCountry(ec, "USA", "United States of America");
			country.setCapital(city);
			try {
				// make sure blobs work
				NSData flag = new NSData(CUDTest.class.getResourceAsStream("/us.png"), 1024);
				country.setFlag(flag);
			} catch (IOException e) {
				log.error(ExceptionUtils.getStackTrace(e), e);
				throw new RuntimeException(e.getMessage(), e);
			}
			ec.saveChanges();
			cnt = ERXEOControlUtilities.objectCountWithQualifier(ec, Country.ENTITY_NAME, Country.CODE.eq("USA"));

			assertNotNull(cnt);
			assertEquals(Integer.valueOf(1), cnt);
		} finally {
			ec.unlock();
		}
	}

	@Test
	public void testUpdate() {
		ERXEC ec = (ERXEC) ERXEC.newEditingContext();
		ec.lock();
		try {
			Country country = Country.fetchCountry(ec, Country.CODE.eq("NLD"));
			final String headOfState = "Queen " + country.headOfState(); 
			country.setHeadOfState(headOfState);
			ec.saveChanges();

			Integer cnt = ERXEOControlUtilities.objectCountWithQualifier(ec, Country.ENTITY_NAME, Country.HEAD_OF_STATE.eq(headOfState));

			assertNotNull(cnt);
			assertEquals(Integer.valueOf(1), cnt);
		} finally {
			ec.unlock();
		}

	}

	@Test
	public void testDelete() {
		ERXEC ec = (ERXEC) ERXEC.newEditingContext();
		ec.lock();
		try {
			Country country = Country.fetchCountry(ec, Country.CODE.eq("NOR"));
			country.delete();
			ec.saveChanges();

			Integer cnt = ERXEOControlUtilities.objectCountWithQualifier(ec, Country.ENTITY_NAME, Country.CODE.eq("NOR"));

			assertNotNull(cnt);
			assertEquals(Integer.valueOf(0), cnt);
		} finally {
			ec.unlock();
		}

	}
}
