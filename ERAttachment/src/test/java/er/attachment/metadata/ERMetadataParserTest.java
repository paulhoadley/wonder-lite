package er.attachment.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Test;

public class ERMetadataParserTest {
  private static File resource(String name) throws URISyntaxException {
    return new File(ERMetadataParserTest.class.getResource("/" + name).toURI());
  }

  private static void assertDimensions(String fileName) throws Exception {
    ERMetadataDirectorySet metadata = ERMetadataParser.metadataParser().parseMetadata(resource(fileName));
    assertFalse(metadata.isEmpty());
    assertEquals(640, metadata.getWidth());
    assertEquals(361, metadata.getHeight());
  }

  @Test
  public void parsesJpegDimensions() throws Exception {
    assertDimensions("a.jpg");
  }

  @Test
  public void parsesPngDimensions() throws Exception {
    assertDimensions("a.png");
  }
}
