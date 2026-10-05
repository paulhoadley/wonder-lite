package er.extensions.eof;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSQLExpressionFactory;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSSet;

import er.erxtest.EOFTestCase;
import er.erxtest.ERXTestUtilities;
import er.erxtest.model.Company;
import er.erxtest.model.Employee;

/**
 * Test the static methods in the ERXEOAccessUtilities class. This class has an inner
 * class that actually contains the test methods. We can take the available models
 * (or the ones compatible with these tests) and run the tests multiple times. The
 * tests can be parameterized to use different models or different configurations.
 */
public class ERXEOAccessUtilitiesTest extends EOFTestCase {

	private EOEditingContext ec;

	private EOModel model;

	private EOEntity companyEntity;
	private EOEntity employeeEntity;

	@BeforeEach
	public void setUp() throws Exception {

		// System.out.println("ERXEOAccessUtilitiesTest.setUp: setup");

		ec = ERXEC.newEditingContext();

		model = EOModelGroup.defaultGroup().modelNamed(MODEL_NAME);

		companyEntity = model.entityNamed(Company.ENTITY_NAME);
		employeeEntity = model.entityNamed(Employee.ENTITY_NAME);
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#entityMatchingString(com.webobjects.eocontrol.EOEditingContext, java.lang.String)}.
	 */
	@Test
	public void testEntityMatchingString() {
		assertEquals(companyEntity, ERXEOAccessUtilities.entityMatchingString(ec, "SomeCompanyPlace"));
		assertEquals(employeeEntity, ERXEOAccessUtilities.entityMatchingString(ec, "SomeEmployeeThing"));
		assertEquals(companyEntity, ERXEOAccessUtilities.entityMatchingString(ec, "CompanyThing"));
		assertEquals(companyEntity, ERXEOAccessUtilities.entityMatchingString(ec, "ThatCompany"));
		assertEquals(companyEntity, ERXEOAccessUtilities.entityMatchingString(ec, "Company"));
		assertNull(ERXEOAccessUtilities.entityMatchingString(ec, "SomeGarbage"));
		assertNull(ERXEOAccessUtilities.entityMatchingString(ec, "null"));
		assertNull(ERXEOAccessUtilities.entityMatchingString(null, "SomeThing"));
		assertNull(ERXEOAccessUtilities.entityMatchingString(null, null));
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#entityUsingTable(com.webobjects.eocontrol.EOEditingContext, java.lang.String)}.
	 */
	@Test
	public void testEntityUsingTable() {
		assertEquals(companyEntity, ERXEOAccessUtilities.entityUsingTable(ec, "Company"));
		assertNull(ERXEOAccessUtilities.entityUsingTable(ec, "GarbageName"));
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#entityWithNamedIsShared(com.webobjects.eocontrol.EOEditingContext, java.lang.String)}.
	 */
	@Test
	public void testEntityWithNamedIsShared() {
		assertFalse(ERXEOAccessUtilities.entityWithNamedIsShared(ec, Company.ENTITY_NAME));
		assertFalse(ERXEOAccessUtilities.entityWithNamedIsShared(ec, Employee.ENTITY_NAME));
		assertFalse(ERXEOAccessUtilities.entityWithNamedIsShared(null, Company.ENTITY_NAME));

		try {
			@SuppressWarnings("unused")
			boolean check = ERXEOAccessUtilities.entityWithNamedIsShared(ec, null);
			fail();
		}
		catch (java.lang.IllegalStateException ise) { /* Good! */
		}
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#entityNamed(com.webobjects.eocontrol.EOEditingContext, java.lang.String)}.
	 */
	@Test
	public void testEntityNamed() {
		assertEquals(companyEntity, ERXEOAccessUtilities.entityNamed(ec, Company.ENTITY_NAME));
		assertEquals(employeeEntity, ERXEOAccessUtilities.entityNamed(ec, Employee.ENTITY_NAME));
		assertEquals(companyEntity, ERXEOAccessUtilities.entityNamed(null, Company.ENTITY_NAME));
		assertNull(ERXEOAccessUtilities.entityNamed(ec, null));
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#modelGroup(com.webobjects.eocontrol.EOEditingContext)}.
	 */
	@Test
	public void testModelGroup() {
		assertEquals(EOModelGroup.defaultGroup(), ERXEOAccessUtilities.modelGroup(ec));
		assertEquals(EOModelGroup.defaultGroup(), ERXEOAccessUtilities.modelGroup(null));
	}

	/**
	 * Test method for {@link er.extensions.eof.ERXEOAccessUtilities#rawRowsForSQLExpression(com.webobjects.eocontrol.EOEditingContext, java.lang.String, com.webobjects.eoaccess.EOSQLExpression)}.
	 * Not the best test, but it will work. Uses the select statement from the method being tested to fetch from the ERXTest model. Then, after inserting
	 * a few objects, makes sure that the results are greater than the last time.
	 */
	@Test
	public void testRawRowsForSQLExpressionEOEditingContextStringEOSQLExpression() {

		EOAdaptor adaptor = EOAdaptor.adaptorWithName(EOModelGroup.defaultGroup().modelNamed(MODEL_NAME).adaptorName());
		assumeFalse("Memory".equals(adaptor.name()), "The Memory adaptor doesn't run SQL");
		EOSQLExpressionFactory factory = new EOSQLExpressionFactory(adaptor);
		NSArray<EOEntity> entities = EOModelGroup.defaultGroup().modelNamed(MODEL_NAME).entities();
		NSMutableDictionary<String,Number> counts = new NSMutableDictionary<>();

		for (EOEntity entity : entities) {
			EOSQLExpression sqlExp = factory.selectStatementForAttributes(entity.attributes(),
								false,
								new EOFetchSpecification(entity.name(), null, null),
								entity);
			NSArray rows = ERXEOAccessUtilities.rawRowsForSQLExpression(ec, MODEL_NAME, sqlExp);

			counts.setObjectForKey(rows.size(), entity.name());
		}

		ERXTestUtilities.createCompanyAnd3Employees();

		NSMutableSet<String> hasMore = new NSMutableSet<>();

		for (EOEntity entity : entities) {
			EOSQLExpression sqlExp = factory.selectStatementForAttributes(entity.attributes(),
								false,
								new EOFetchSpecification(entity.name(), null, null),
								entity);
			NSArray rows = ERXEOAccessUtilities.rawRowsForSQLExpression(ec, MODEL_NAME, sqlExp);

			if ( ! counts.containsKey(entity.name()))
				fail();
			if (counts.objectForKey(entity.name()).intValue() > rows.size())
				fail();
			if (counts.objectForKey(entity.name()).intValue() < rows.size())
				hasMore.add(entity.name());
		}

		assertEquals(new NSSet<>(new String[] { "Company", "Employee", "Paycheck" } ), hasMore);
	}

}
