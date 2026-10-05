package er.erxtest.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.erxtest.EOFTestCase;
import er.erxtest.model.Company;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEnterpriseObjectCache;

public class ERXEnterpriseObjectCacheTestCase extends EOFTestCase {

  private EOEditingContext editingContext;
  private Company c1, c2;

  @Test
  public void testInsert() {
    testInsert(true);
  }

  @Test
  public void testUpdate() {
    testUpdate(true);
  }

  @Test
  public void testDelete() {
    testDelete(true);
  }

  @Test
  public void testInsertNoFetchInitialValues() {
    testInsert(false);
  }

  @Test
  public void testUpdateNoFetchInitialValues() {
    testUpdate(false);
  }

  @Test
  public void testDeleteNoFetchInitialValues() {
    testDelete(false);
  }

  @Test
  public void testFetchWithFetchInitialValues() {
    ERXEnterpriseObjectCache<Company> cache = new ERXEnterpriseObjectCache<>(Company.ENTITY_NAME, Company.NAME_KEY, null, 0);
    Company c1Test = cache.objectForKey(editingContext, c1.name());
    assertSame(c1, c1Test);
    cache.stop();
  }

  @Test
  public void testFetchWithFetchInitialValuesAndTimeout() {
      ERXEnterpriseObjectCache<Company> cache = new ERXEnterpriseObjectCache<>(Company.ENTITY_NAME, Company.NAME_KEY, null, 100000);
      Company c1Test = cache.objectForKey(editingContext, c1.name());
      assertSame(c1, c1Test);
      cache.stop();
  }

  @Test
  public void testFetchNoFetchInitialValues() {
    ERXEnterpriseObjectCache<Company> cache = new ERXEnterpriseObjectCache<>(Company.ENTITY_NAME, Company.NAME_KEY, null, 0);
    cache.setFetchInitialValues(false);
    Company c1Test = cache.objectForKey(editingContext, c1.name());
    assertSame(c1, c1Test);
    cache.stop();
  }

  @Test
  public void testFetchNoFetchInitialValuesAndTimeout() {

    ERXEnterpriseObjectCache<Company> cache = new ERXEnterpriseObjectCache<>(Company.ENTITY_NAME, Company.NAME_KEY, null, 100000);
    cache.setFetchInitialValues(false);
    Company c1Test = cache.objectForKey(editingContext, c1.name());
    assertSame(c1, c1Test);
    cache.stop();
  }

  @Test
  public void testFetchWithQualifier() {
    ERXEnterpriseObjectCache<Company> cache = new ERXEnterpriseObjectCache<>(Company.ENTITY_NAME, Company.NAME_KEY, Company.NAME.contains("Test"), 0);
    cache.setFetchInitialValues(false);
    Company c1Test = cache.objectForKey(editingContext, c1.name());
    assertNull(c1Test);
    Company c2Test = cache.objectForKey(editingContext, c2.name());
    assertSame(c2, c2Test);
    cache.stop();
  }

  @Test
  public void testUpdateWithQualifier() {
    ERXEnterpriseObjectCache<Company> cache = new ERXEnterpriseObjectCache<>(Company.ENTITY_NAME, Company.NAME_KEY, Company.NAME.contains("Test"), 0);
    cache.setFetchInitialValues(false);
    Company c1Test = cache.objectForKey(editingContext, c1.name());
    assertNull(c1Test);
    Company c2Test = cache.objectForKey(editingContext, c2.name());
    assertSame(c2, c2Test);

    String name1Update = "Another Test Company " + UUID.randomUUID().toString();
    c1.setName(name1Update);
    editingContext.saveChanges();

    Company c1UpdateTest = cache.objectForKey(editingContext, name1Update);
    assertSame(c1, c1UpdateTest);

    String name2Update = "Company 2 " + UUID.randomUUID().toString();
    c2.setName(name2Update);
    editingContext.saveChanges();

    Company c2UpdateTest = cache.objectForKey(editingContext, name2Update);
    assertNull(c2UpdateTest);

    cache.stop();
  }

  public void testInsert(boolean fetchInitialValues) {
    ERXEnterpriseObjectCache<Company> cache = new ERXEnterpriseObjectCache<>(Company.ENTITY_NAME, Company.NAME_KEY, null, 0);
    cache.setFetchInitialValues(fetchInitialValues);
    Company c1Test = cache.objectForKey(editingContext, c1.name());
    assertSame(c1, c1Test);
    cache.stop();
  }

  public void testDelete(boolean fetchInitialValues) {
    ERXEnterpriseObjectCache<Company> cache = new ERXEnterpriseObjectCache<>(Company.ENTITY_NAME, Company.NAME_KEY, null, 0);
    cache.setFetchInitialValues(fetchInitialValues);
    String name = c1.name();
    Company c1Test = cache.objectForKey(editingContext, name);
    assertSame(c1, c1Test);
    c1.delete();
    editingContext.saveChanges();
    c1= null;
    Company c1DeleteTest = cache.objectForKey(editingContext, name);
    assertNull(c1DeleteTest);
    cache.stop();
  }

  public void testUpdate(boolean fetchInitialValues) {
    ERXEnterpriseObjectCache<Company> cache = new ERXEnterpriseObjectCache<>(Company.ENTITY_NAME, Company.NAME_KEY, null, 0);
    cache.setFetchInitialValues(fetchInitialValues);
    String name1 = c1.name();

    String name2 = "Company " + UUID.randomUUID().toString();
    c1.setName(name2);
    editingContext.saveChanges();

    Company c1DeleteTest = cache.objectForKey(editingContext, name1);
    assertNull(c1DeleteTest);

    Company c2UpdateTest = cache.objectForKey(editingContext, name2);
    assertSame(c1, c2UpdateTest);
    cache.stop();
  }

  @Test
  public void testAllObjects() {
      ERXEnterpriseObjectCache<Company> cache = new ERXEnterpriseObjectCache<>(Company.ENTITY_NAME, Company.NAME_KEY, null, 0);
      cache.setFetchInitialValues(true);

      NSArray allObjects = cache.allObjects(editingContext);
      assertTrue(allObjects.containsObject(c1));
      assertTrue(allObjects.containsObject(c2));
      
      allObjects = cache.allObjects(editingContext, Company.NAME.contains("Test"));
      assertFalse(allObjects.containsObject(c1));
      assertTrue(allObjects.containsObject(c2));
      
      cache.stop();
    }
  
  @BeforeEach
  public void setUp() throws Exception {
      String name1 = "Company " + UUID.randomUUID().toString();
      editingContext = ERXEC.newEditingContext();
      c1 = Company.createCompany(editingContext, name1);
      editingContext.saveChanges();

      String name2 = "Company Test " + UUID.randomUUID().toString();
      c2 = Company.createCompany(editingContext, name2);
      editingContext.saveChanges();
  }

  @AfterEach
  public void tearDown() throws Exception {
      if (c1 != null) editingContext.deleteObject(c1);
      if (c2 != null) editingContext.deleteObject(c2);
      editingContext.saveChanges();
      editingContext.dispose();
      editingContext = null;
  }

}
