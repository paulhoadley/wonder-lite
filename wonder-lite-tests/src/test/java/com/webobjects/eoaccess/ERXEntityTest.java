
package com.webobjects.eoaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSSet;

import er.erxtest.EOFTestCase;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXModelGroup;

/* Test the ERXEntity methods. These tests are extremely minimal.
 * This class exists in order to deal with some issues in inheritance, and
 * this system is not configured to use models that use inheritance yet.
 *
 * @author Ray Kiddy, ray@ganymede.org
 */
public class ERXEntityTest extends EOFTestCase {

    static String buildRoot;
    static {
        buildRoot = System.getProperty("build.root");
    }
    EOEditingContext ec;
    String adaptorName = "Memory";
    String modelName;
    EOModel model;

    @BeforeEach
    public void setUp() throws Exception {

//        if (ec != null) ec.dispose();
//        if (model != null) model.dispose();
//
//        EOModelGroup.setDefaultGroup(new EOModelGroup());
//
//        modelName = "ERXTest";
//
//        URL modelUrl = ERXFileUtilities.pathURLForResourceNamed(modelName+".eomodeld", null, null);
//
//        EOModelGroup.defaultGroup().addModel(new EOModel(modelUrl));
//
//        model = EOModelGroup.defaultGroup().modelNamed(modelName);
//        model.setConnectionDictionary(ERExtensionsTest.connectionDict(adaptorName));

        model = EOModelGroup.defaultGroup().modelNamed("ERXTest");
        ec = ERXEC.newEditingContext();
    }

    @Test
    public void testConstructor() {
        ERXEntity entity = new ERXEntity();
        assertNotNull(entity);
    }

    @Test
    public void testPlistConstructor() {
        URL entityUrl = null;
        try {
            entityUrl = new java.net.URL(model.pathURL()+"/Company.plist");
        } catch (java.net.MalformedURLException e) { throw new IllegalArgumentException(e.getMessage()); }

        NSDictionary plist = (NSDictionary)NSPropertyListSerialization.propertyListWithPathURL(entityUrl);

        assertNotNull(new ERXEntity(plist, model));
    }

    @Test
    public void testClassAttributes() {
        NSArray<EOAttribute> attrs = ((ERXEntity)ERXModelGroup.defaultGroup().entityNamed("Employee")).classAttributes();
        @SuppressWarnings("unchecked")
        NSSet<String> foundAttrs = new NSSet<>((NSArray<String>)attrs.valueForKey("name"));
	NSSet<String> expectedAttrs = new NSSet<>(new String[] {
           "address1", "address2", "bestSalesTotal", "city", "firstName", "lastName", "manager", "state", "zipcode"
                                                                     } );
        assertEquals(expectedAttrs, foundAttrs);
    }

    @Test
    public void testClassRelationships() {
        NSArray<EORelationship> rels = ((ERXEntity)ERXModelGroup.defaultGroup().entityNamed("Employee")).classRelationships();
        @SuppressWarnings("unchecked")
        NSSet<String> foundRels = new NSSet<>((NSArray<String>)rels.valueForKey("name"));
        NSSet<String> expectedRels = new NSSet<>(new String[] { "company", "department", "paychecks", "roles" } );
        assertEquals(expectedRels, foundRels);
    }

    @Test
    public void testHasExternalName() {
        URL entityUrl = null;
        try {
            entityUrl = new java.net.URL(model.pathURL()+"/Company.plist");
        } catch (java.net.MalformedURLException e) { throw new IllegalArgumentException(e.getMessage()); }

        NSDictionary plist = (NSDictionary)NSPropertyListSerialization.propertyListWithPathURL(entityUrl);

        ERXEntity erxentity = new ERXEntity(plist, model);

        assertTrue(erxentity.hasExternalName());
    }

    @Test
    public void testSetClassDescription() {

        EOEntity entity1 = EOModelGroup.defaultGroup().entityNamed("Company");
        EOClassDescription desc = entity1.classDescriptionForInstances();

        assertNotNull(desc);

        URL entityUrl = null;
        try {
            entityUrl = new java.net.URL(model.pathURL()+"/Employee.plist");
        } catch (java.net.MalformedURLException e) { throw new IllegalArgumentException(e.getMessage()); }

        NSDictionary plist = (NSDictionary)NSPropertyListSerialization.propertyListWithPathURL(entityUrl);

        ERXEntity entity2 = new ERXEntity(plist, model);

        // Using a mis-matched EOClassDescription here, but doing that on purpose so we can verify the superclass did not just ignore the set.
        //
        entity2.setClassDescription(desc);

        //assertTrue(ERExtensionsTest.equalsForEOAccessObjects(desc, entity2.classDescriptionForInstances()));
    }
}
