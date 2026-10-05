package er.memoryadaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * Exercises the adaptor through its channel, using a model built in code, so no EOF
 * stack or application is needed.
 *
 * @since 0.2
 */
public class ERMemoryAdaptorChannelTest {
  private EOEntity entity;
  private ERMemoryAdaptorContext context;
  private EOAdaptorChannel channel;

  @Before
  public void setUp() {
    EOModel model = new EOModel();
    model.setName("MemoryTest");
    model.setAdaptorName("Memory");
    entity = new EOEntity();
    entity.setName("Person");
    entity.setExternalName("PERSON");
    entity.setClassName("EOGenericRecord");
    EOAttribute id = attribute("id", "ID", "java.lang.Integer", "i");
    entity.addAttribute(id);
    entity.addAttribute(attribute("name", "NAME", "java.lang.String", null));
    entity.setPrimaryKeyAttributes(new NSArray<>(id));
    model.addEntity(entity);

    context = (ERMemoryAdaptorContext) new ERMemoryAdaptor("Memory").createAdaptorContext();
    context.resetAllEntities();
    channel = context.createAdaptorChannel();
    channel.openChannel();
    insert(1, "Alice");
    insert(2, "Bob");
  }

  @After
  public void tearDown() {
    channel.closeChannel();
    context.resetAllEntities();
  }

  private static EOAttribute attribute(String name, String columnName, String className, String valueType) {
    EOAttribute attribute = new EOAttribute();
    attribute.setName(name);
    attribute.setColumnName(columnName);
    attribute.setClassName(className);
    if (valueType != null) {
      attribute.setValueType(valueType);
    }
    return attribute;
  }

  private void insert(int id, String name) {
    channel.insertRow(new NSDictionary<>(new Object[] { Integer.valueOf(id), name }, new String[] { "id", "name" }), entity);
  }

  private NSArray<String> fetchNames(EOQualifier qualifier) {
    channel.selectAttributes(entity.attributes(), new EOFetchSpecification("Person", qualifier, null), false, entity);
    NSMutableArray<String> names = new NSMutableArray<>();
    NSMutableDictionary row;
    while ((row = channel.fetchRow()) != null) {
      names.addObject((String) row.objectForKey("name"));
    }
    return names;
  }

  private static EOQualifier idIs(int id) {
    return EOQualifier.qualifierWithQualifierFormat("id = %@", new NSArray<>(Integer.valueOf(id)));
  }

  @Test
  public void fetchesInsertedRows() {
    NSArray<String> names = fetchNames(null);
    assertEquals(2, names.count());
    assertTrue(names.containsObject("Alice"));
    assertTrue(names.containsObject("Bob"));
  }

  @Test
  public void fetchesRowsMatchingQualifier() {
    assertEquals(new NSArray<>("Bob"), fetchNames(EOQualifier.qualifierWithQualifierFormat("name = 'Bob'", null)));
  }

  @Test
  public void updatesMatchingRows() {
    int updated = channel.updateValuesInRowsDescribedByQualifier(new NSDictionary<>("Robert", "name"), idIs(2), entity);
    assertEquals(1, updated);
    assertEquals(new NSArray<>("Robert"), fetchNames(idIs(2)));
  }

  @Test
  public void deletesMatchingRows() {
    int deleted = channel.deleteRowsDescribedByQualifier(idIs(1), entity);
    assertEquals(1, deleted);
    assertEquals(new NSArray<>("Bob"), fetchNames(null));
  }

  @Test
  public void discardsRolledBackInserts() {
    context.beginTransaction();
    insert(3, "Carol");
    context.rollbackTransaction();
    assertEquals(2, fetchNames(null).count());
  }

  @Test
  public void resetEntityRemovesItsRows() {
    context.resetEntity(entity);
    assertEquals(0, fetchNames(null).count());
  }
}
