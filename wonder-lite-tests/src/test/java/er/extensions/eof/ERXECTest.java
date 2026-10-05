
package er.extensions.eof;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSData;

import er.erxtest.EOFTestCase;
import er.erxtest.model.Company;
import er.erxtest.model.Employee;

public class ERXECTest extends EOFTestCase {

    @Test
    public void testConstructor() {
        assertNotNull(new ERXEC());
    }

    @Test
    public void testConstructorWithObjectStore() {

        EOEditingContext parentEC1 = new EOEditingContext();
        EOEditingContext parentEC2 = new EOEditingContext();

        ERXEC ec1 = new ERXEC(parentEC1);
        assertNotNull(ec1);

        ERXEC ec2 = new ERXEC(parentEC2);
        assertNotNull(ec2);

        assertEquals(parentEC1, ec1.parentObjectStore());
        assertEquals(parentEC2, ec2.parentObjectStore());

        ERXEC parentEC3 = new ERXEC();

        ERXEC ec3 = new ERXEC(parentEC3);
        assertNotNull(ec3);

        assertEquals(parentEC3, ec3.parentObjectStore());
    }
    
    @Test
    public void testNestedECs() {
    	try {
	    	EOEditingContext ec = ERXEC.newEditingContext();
    		Company c = (Company) EOUtilities.createAndInsertInstance(ec, Company.ENTITY_NAME);
    		c.setName("Name");
    		ec.saveChanges();
    		EOEditingContext nested = ERXEC.newEditingContext(ec);
    		Company nestC = c.localInstanceIn(nested);
    		Employee e = (Employee) EOUtilities.createAndInsertInstance(nested, Employee.ENTITY_NAME);
    		e.setFirstName("First");
    		e.setLastName("Last");
    		e.setManager(Boolean.FALSE);
    		e.addObjectToBothSidesOfRelationshipWithKey(nestC, Employee.COMPANY_KEY);
    		nested.saveChanges();
    		ec.saveChanges();
	    	System.gc();
    		c.delete();
    		ec.saveChanges();
    	} catch (Exception e) {
    		e.printStackTrace();
    		fail(e.getMessage());
    	}
    }
    
    @Test
    public void testSerializablilty() throws IOException, ClassNotFoundException {
		EOEditingContext ec = ERXEC.newEditingContext();
		Company.createCompany(ec, "Some fruit company");
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		NSData data = null;

		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(ec);
			oos.flush();
			byte[] bytes = baos.toByteArray();
			data = new NSData(bytes);
		} finally {
			if (oos != null) {
				oos.close();
			}
		}
		
		Object object = null;
		byte[] bytes = data.bytes();
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
			object = ois.readObject();
		} finally {
			if (ois != null) {
				ois.close();
			}
		}

		EOEditingContext ec2 = (EOEditingContext) object;
		
		assertNotSame(ec, ec2);
		ec.dispose();
		ec2.saveChanges();
		// Not made by ERXEC's factory, so EOFTestCase won't dispose of it
		ec2.dispose();
    }

}
