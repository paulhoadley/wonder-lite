package er.erxtest.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;

import er.erxtest.EOFTestCase;
import er.erxtest.model.Company;
import er.erxtest.model.Employee;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXGenericRecord;

public class ERXGenericRecordUpdateInverseRelationshipsTest extends EOFTestCase {
  @BeforeEach
  public void setUp() throws Exception {
    ERXGenericRecord.InverseRelationshipUpdater.setUpdateInverseRelationships(true);
  }
  
  @AfterEach
  public void tearDown() throws Exception {
    ERXGenericRecord.InverseRelationshipUpdater.setUpdateInverseRelationships(false);
  }
  
  @Test
  public void testSetToOneImplicitToMany() {
    ERXGenericRecord.InverseRelationshipUpdater.setUpdateInverseRelationships(false);
    EOEditingContext editingContext = ERXEC.newEditingContext();
    Company company = Company.createCompany(editingContext, "XYZ");
    Employee p1 = (Employee) EOUtilities.createAndInsertInstance(editingContext, Employee.ENTITY_NAME);
    p1.setCompanyRelationship(company);

    assertEquals(company, p1.company());
    assertEquals(1, company.employees().count());
    assertEquals(p1, company.employees().objectAtIndex(0));
  }

  @Test
  public void testAddToToManyImplicitToOne() {
    ERXGenericRecord.InverseRelationshipUpdater.setUpdateInverseRelationships(false);
    EOEditingContext editingContext = ERXEC.newEditingContext();
    Company company = Company.createCompany(editingContext, "XYZ");
    Employee p1 = (Employee) EOUtilities.createAndInsertInstance(editingContext, Employee.ENTITY_NAME);
    company.addToEmployeesRelationship(p1);

    assertEquals(company, p1.company());
    assertEquals(1, company.employees().count());
    assertEquals(p1, company.employees().objectAtIndex(0));
  }

  @Test
  public void testAutoSetToOneImplicitToMany() {
    EOEditingContext editingContext = ERXEC.newEditingContext();
    Company company = Company.createCompany(editingContext, "XYZ");
    Employee p1 = (Employee) EOUtilities.createAndInsertInstance(editingContext, Employee.ENTITY_NAME);
    p1.setCompany(company);

    assertEquals(company, p1.company());
    assertEquals(1, company.employees().count());
    assertEquals(p1, company.employees().objectAtIndex(0));
  }

  @Test
  public void testAutoAddToToManyImplicitToOne() {
    EOEditingContext editingContext = ERXEC.newEditingContext();
    Company company = Company.createCompany(editingContext, "XYZ");
    Employee p1 = (Employee) EOUtilities.createAndInsertInstance(editingContext, Employee.ENTITY_NAME);
    company.addToEmployees(p1);

    assertEquals(company, p1.company());
    assertEquals(1, company.employees().count());
    assertEquals(p1, company.employees().objectAtIndex(0));
  }

  @Test
  public void testAutoSetThenSetToOneToNullImplicitToMany() {
    EOEditingContext editingContext = ERXEC.newEditingContext();
    Company company = Company.createCompany(editingContext, "XYZ");
    Employee p1 = (Employee) EOUtilities.createAndInsertInstance(editingContext, Employee.ENTITY_NAME);
    p1.setCompany(company);
    p1.setCompany(null);
   
    assertEquals(null, p1.company());
    assertEquals(0, company.employees().count());
  }

  @Test
  public void testAutoSetThenSetToOneToAnotherImplicitToMany() {
    EOEditingContext editingContext = ERXEC.newEditingContext();
    Company company = Company.createCompany(editingContext, "XYZ");
    Company company2 = Company.createCompany(editingContext, "XYZ");
    Employee p1 = (Employee) EOUtilities.createAndInsertInstance(editingContext, Employee.ENTITY_NAME);
    p1.setCompany(company);
    p1.setCompany(company2);

    assertEquals(company2, p1.company());
    assertEquals(0, company.employees().count());
    assertEquals(1, company2.employees().count());
    assertEquals(p1, company2.employees().objectAtIndex(0));
  }

  @Test
  public void testAutoRemoveFromToManyImplicitToOne() {
    EOEditingContext editingContext = ERXEC.newEditingContext();
    Company company = Company.createCompany(editingContext, "XYZ");
    Employee p1 = (Employee) EOUtilities.createAndInsertInstance(editingContext, Employee.ENTITY_NAME);
    company.addToEmployees(p1);
    company.removeFromEmployees(p1);

    assertEquals(null, p1.company());
    assertEquals(0, company.employees().count());
  }

  @Test
  public void testAutoSetTwoToOnesImplicitToMany() {
    EOEditingContext editingContext = ERXEC.newEditingContext();
    Company company = Company.createCompany(editingContext, "XYZ");
    Employee p1 = (Employee) EOUtilities.createAndInsertInstance(editingContext, Employee.ENTITY_NAME);
    Employee p2 = (Employee) EOUtilities.createAndInsertInstance(editingContext, Employee.ENTITY_NAME);
    p1.setCompany(company);
    p2.setCompany(company);

    assertEquals(company, p1.company());
    assertEquals(company, p2.company());
    assertEquals(2, company.employees().count());
    assertTrue(company.employees().containsObject(p1));
    assertTrue(company.employees().containsObject(p2));
  }

  @Test
  public void testAutoRemoveFromToManyWithManyImplicitToOne() {
    EOEditingContext editingContext = ERXEC.newEditingContext();
    Company company = Company.createCompany(editingContext, "XYZ");
    Employee p1 = (Employee) EOUtilities.createAndInsertInstance(editingContext, Employee.ENTITY_NAME);
    Employee p2 = (Employee) EOUtilities.createAndInsertInstance(editingContext, Employee.ENTITY_NAME);
    company.addToEmployees(p1);
    company.addToEmployees(p2);
    company.removeFromEmployees(p1);

    assertEquals(null, p1.company());
    assertEquals(company, p2.company());
    assertEquals(1, company.employees().count());
    assertEquals(p2, company.employees().objectAtIndex(0));
  }
  
  @Test
  public void testRevertToManyWithoutInverseRelationshipUpdating() {
    testRevertToMany(false);
  }
  
  @Test
  public void testRevertToManyWithInverseRelationshipUpdating() {
    testRevertToMany(true);
  }
  
  public void testRevertToMany(boolean updateInverseRelationships) {
    ERXGenericRecord.InverseRelationshipUpdater.setUpdateInverseRelationships(updateInverseRelationships);
    EOEditingContext editingContext = ERXEC.newEditingContext();
    Company company = Company.createCompany(editingContext, "XYZ");
    editingContext.saveChanges();
    
    Employee p1 = (Employee) EOUtilities.createAndInsertInstance(editingContext, Employee.ENTITY_NAME);
    p1.setCompanyRelationship(company);

    assertEquals(company, p1.company());
    assertEquals(1, company.employees().count());
    assertEquals(p1, company.employees().objectAtIndex(0));
    
    editingContext.revert();
    
    assertEquals(0, company.employees().count());
  }
}
