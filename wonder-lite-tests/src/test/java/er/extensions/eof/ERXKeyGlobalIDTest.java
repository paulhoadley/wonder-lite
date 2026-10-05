
package er.extensions.eof;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.foundation.NSData;

import er.erxtest.EOFTestCase;
import er.erxtest.ERXTestUtilities;
import er.erxtest.model.Company;

/**
 * Tests of the {@link er.extensions.eof.ERXKeyGlobalID} class. Methods in this class rely on the fact
 * that the objects being used while testing, those being Company and sometimes Employee, have a
 * single-attribute primary key.
 */
public class ERXKeyGlobalIDTest extends EOFTestCase {

	private EOEditingContext ec;
	private Company co;

	@BeforeEach
	public void setUp() throws Exception {

		ec = ERXEC.newEditingContext();
		co = Company.createCompany(ec, "Foobar.com");
		ec.saveChanges();
	}

	@AfterEach
	public void tearDown() throws Exception {

//		ec.deleteObject(co);
//		ec.saveChanges();		
	}

	@Test
	public void testConstructor() {

		ERXKeyGlobalID xkgid = new ERXKeyGlobalID(Company.ENTITY_NAME, EOUtilities.primaryKeyForObject(ec, co).values().toArray());
		assertNotNull(xkgid);
	}

	@Test
	public void testFromData() {

		int pk = ERXTestUtilities.pkOne(ec, co);
		
		ERXKeyGlobalID xkgid = ERXKeyGlobalID.fromData(new NSData((Company.ENTITY_NAME+"."+pk).getBytes()));
		assertNotNull(xkgid);
	}

	@Test
	public void testFromString() {
	
		int pk = ERXTestUtilities.pkOne(ec, co);

		ERXKeyGlobalID xkgid = ERXKeyGlobalID.fromString(Company.ENTITY_NAME+"."+pk);
		assertNotNull(xkgid);
	}

	@SuppressWarnings("boxing")
	@Test
	public void testEquals() {

		EOKeyGlobalID kgid1 = EOKeyGlobalID.globalIDWithEntityName(Company.ENTITY_NAME, new Integer[] { ERXTestUtilities.pkOne(ec, co) } ); 
		
		ERXKeyGlobalID xkgid1 = ERXKeyGlobalID.globalIDForGID(kgid1);
		ERXKeyGlobalID xkgid2 = ERXKeyGlobalID.globalIDForGID(kgid1);

		assertFalse(kgid1.equals(xkgid1));
		assertFalse(xkgid1.equals(kgid1));

		assertTrue(xkgid1.equals(xkgid2));
		assertTrue(xkgid2.equals(xkgid1));
		
		EOEditingContext ec2 = ERXEC.newEditingContext();
		EOEnterpriseObject co2 = ERXEOControlUtilities.localInstanceOfObject(ec2, co);

		EOKeyGlobalID kgid2 = EOKeyGlobalID.globalIDWithEntityName(Company.ENTITY_NAME, new Integer[] { ERXTestUtilities.pkOne(ec2, co2) } ); 
		ERXKeyGlobalID xkgid3 = ERXKeyGlobalID.globalIDForGID(kgid2);

		assertFalse(kgid2.equals(xkgid3));
		assertFalse(xkgid3.equals(kgid2));

		assertTrue(xkgid1.equals(xkgid3));
		assertTrue(xkgid3.equals(xkgid1));	
	}

	@Test
	public void testGlobalIDForGID() {

		EOGlobalID gid = ec.globalIDForObject(co);

		ERXKeyGlobalID xkgid1 = ERXKeyGlobalID.globalIDForGID((EOKeyGlobalID)gid);
		assertEquals(gid, xkgid1.globalID());
	}

	@SuppressWarnings("boxing")
	@Test
	public void testGlobalID() {

		EOGlobalID gid = ec.globalIDForObject(co);

		int pk = ERXTestUtilities.pkOne(ec, co);

		ERXKeyGlobalID xkgid1 = new ERXKeyGlobalID(Company.ENTITY_NAME, new Integer[] { pk });
		assertEquals(gid, xkgid1.globalID());
				
		ERXKeyGlobalID xkgid2 = ERXKeyGlobalID.fromData(new NSData((Company.ENTITY_NAME+"."+pk).getBytes()));
		assertEquals(gid, xkgid2.globalID());

		ERXKeyGlobalID xkgid3 = ERXKeyGlobalID.fromString(Company.ENTITY_NAME+"."+pk);
		assertEquals(gid, xkgid3.globalID());
	}
	
	@Test
	public void test_keyValuesNoCopy() {

		ERXKeyGlobalID xkgid = ERXKeyGlobalID.globalIDForGID((EOKeyGlobalID)ec.globalIDForObject(co));

		// How to test the no-copy-ness? -rrk
		//
		assertArrayEquals(xkgid.keyValues(), xkgid._keyValuesNoCopy());
	}

	@Test
	public void testHashCode() {

		ERXKeyGlobalID xkgid = ERXKeyGlobalID.globalIDForGID((EOKeyGlobalID)ec.globalIDForObject(co));

		// Is there a way to figure out what the hashCode _should_ be without just asking for it?
		// Asking for it would make this test tautological. -rrk
		//
		assertTrue(xkgid.hashCode() != 0);
	}
    
	@Test
	public void testKeyCount() {

		ERXKeyGlobalID xkgid = ERXKeyGlobalID.globalIDForGID((EOKeyGlobalID)ec.globalIDForObject(co));

		assertEquals(1, xkgid.keyCount());
	}
	           
	@SuppressWarnings("boxing")
	@Test
	public void testKeyValues() {

		ERXKeyGlobalID xkgid = ERXKeyGlobalID.globalIDForGID((EOKeyGlobalID)ec.globalIDForObject(co));

		int pk = ERXTestUtilities.pkOne(ec, co);

		Integer[] values = new Integer[] { pk };
		assertArrayEquals(values, xkgid.keyValues());
	}

	@Test
	public void testToString() {

		ERXKeyGlobalID xkgid = ERXKeyGlobalID.globalIDForGID((EOKeyGlobalID)ec.globalIDForObject(co));

		int pk = ERXTestUtilities.pkOne(ec, co);

		assertEquals("_EOIntegralKeyGlobalID["+Company.ENTITY_NAME+" (java.lang.Integer)"+pk+"]", xkgid.toString());
	}
}
