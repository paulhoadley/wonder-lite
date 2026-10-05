package er.erxtest.tests;
import er.erxtest.ERXTestCase;
import er.extensions.foundation.ERXExpiringCache;

public class ERXExpiringCacheTestCase extends ERXTestCase {
  public void testThatItWorksAtAll() {
    ERXExpiringCache<String, String> cache = new ERXExpiringCache<>(100);
    cache.setObjectForKey("Krank", "Anjo");
    cache.setObjectForKey("Schrag", "Mike");
    assertEquals("Schrag", cache.objectForKey("Mike"));
    assertEquals("Krank", cache.objectForKey("Anjo"));
    assertEquals(null, cache.objectForKey("Chuck"));
  }
  
  public void testThatNoTimeoutWorksAtAll() {
    ERXExpiringCache<String, String> noTimeoutCache = new ERXExpiringCache<>(ERXExpiringCache.NO_TIMEOUT);
    noTimeoutCache.setObjectForKey("Krank", "Anjo");
    noTimeoutCache.setObjectForKey("Schrag", "Mike");
    assertEquals("Schrag", noTimeoutCache.objectForKey("Mike"));
    assertEquals("Krank", noTimeoutCache.objectForKey("Anjo"));
    assertEquals(null, noTimeoutCache.objectForKey("Chuck"));
  }
  
  public void testVersionsWithVersionlessGet() {
    ERXExpiringCache<String, String> cache = new ERXExpiringCache<>(ERXExpiringCache.NO_TIMEOUT);
    cache.setObjectForKeyWithVersion("Krank", "Anjo", Integer.valueOf(1));
    cache.setObjectForKeyWithVersion("Schrag", "Mike", Integer.valueOf(1));
    assertEquals("Schrag", cache.objectForKey("Mike"));
    assertEquals("Krank", cache.objectForKey("Anjo"));
    assertEquals(null, cache.objectForKey("Chuck"));
  }

  public void testVersionsWithSameVersionGet() {
    ERXExpiringCache<String, String> cache = new ERXExpiringCache<>(ERXExpiringCache.NO_TIMEOUT);
    cache.setObjectForKeyWithVersion("Krank", "Anjo", Integer.valueOf(1));
    cache.setObjectForKeyWithVersion("Schrag", "Mike", Integer.valueOf(1));
    assertEquals("Schrag", cache.objectForKeyWithVersion("Mike", Integer.valueOf(1)));
    assertEquals("Krank", cache.objectForKeyWithVersion("Anjo", Integer.valueOf(1)));
    assertEquals(null, cache.objectForKeyWithVersion("Chuck", Integer.valueOf(1)));
  }

  public void testVersionsWithNewVersionGetMakeSureItIsStillGone() {
    ERXExpiringCache<String, String> cache = new ERXExpiringCache<>(ERXExpiringCache.NO_TIMEOUT);
    cache.setObjectForKeyWithVersion("Krank", "Anjo", Integer.valueOf(1));
    cache.setObjectForKeyWithVersion("Schrag", "Mike", Integer.valueOf(1));
    assertEquals(null, cache.objectForKeyWithVersion("Mike", Integer.valueOf(2)));
    assertEquals("Krank", cache.objectForKeyWithVersion("Anjo", Integer.valueOf(1)));
    assertEquals(null, cache.objectForKeyWithVersion("Chuck", Integer.valueOf(1)));

    assertEquals(null, cache.objectForKeyWithVersion("Mike", Integer.valueOf(1)));
  }

  public void testVersionsWithNewVersionGet() {
    ERXExpiringCache<String, String> cache = new ERXExpiringCache<>(ERXExpiringCache.NO_TIMEOUT);
    cache.setObjectForKeyWithVersion("Krank", "Anjo", Integer.valueOf(1));
    cache.setObjectForKeyWithVersion("Schrag", "Mike", Integer.valueOf(1));
    assertEquals(null, cache.objectForKeyWithVersion("Mike", Integer.valueOf(2)));
    assertEquals("Krank", cache.objectForKeyWithVersion("Anjo", Integer.valueOf(1)));
    assertEquals(null, cache.objectForKeyWithVersion("Chuck", Integer.valueOf(1)));
  }

  public void testTimeExpiration() {
    // Objects expire after 4 seconds. Lookups check for expiry themselves, so checks
    // for expired objects come just after it, and the rest have 1.5 seconds or more.
    ERXExpiringCache<String, String> cache = new ERXExpiringCache<>(4);
    cache.startBackgroundExpiration();
    
    cache.setObjectForKey("Krank", "Anjo");
    cache.setObjectForKey("Schrag", "Mike");
    assertEquals("Schrag", cache.objectForKey("Mike"));
    assertEquals("Krank", cache.objectForKey("Anjo"));
    assertEquals(null, cache.objectForKey("Chuck"));

    try {
      Thread.sleep(2500);
    }
    catch (Throwable t) {
      t.printStackTrace();
    }
    assertEquals("Schrag", cache.objectForKey("Mike"));
    assertEquals("Krank", cache.objectForKey("Anjo"));
    assertEquals(null, cache.objectForKey("Chuck"));

    cache.setObjectForKey("Hill", "Chuck");
    assertEquals("Schrag", cache.objectForKey("Mike"));
    assertEquals("Krank", cache.objectForKey("Anjo"));
    assertEquals("Hill", cache.objectForKey("Chuck"));

    try {
      Thread.sleep(2000);
    }
    catch (Throwable t) {
      t.printStackTrace();
    }
    assertEquals(null, cache.objectForKey("Mike"));
    assertEquals(null, cache.objectForKey("Anjo"));
    assertEquals("Hill", cache.objectForKey("Chuck"));
    
    try {
      Thread.sleep(2500);
    }
    catch (Throwable t) {
      t.printStackTrace();
    }
    assertEquals(null, cache.objectForKey("Mike"));
    assertEquals(null, cache.objectForKey("Anjo"));
    assertEquals(null, cache.objectForKey("Chuck"));
    
    cache.stopBackgroundExpiration();
  }
}
