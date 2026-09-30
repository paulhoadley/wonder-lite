package er.attachment.thumbnail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.webobjects.foundation.NSArray;

import er.attachment.utils.ERMimeType;

public class ERImageProcessorTest {
  // Apps get this from ERAttachment's Properties at startup; unit tests have no registry.
  private static final ERMimeType JPEG = new ERMimeType("JPEG", "image/jpeg", "public.jpeg", new NSArray<String>(new String[] { "jpg", "jpeg" }));

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  private static File resource(String name) throws URISyntaxException {
    return new File(ERImageProcessorTest.class.getResource("/" + name).toURI());
  }

  private void assertThumbnailFits(String inputName, String outputName) throws Exception {
    File output = new File(folder.getRoot(), outputName);
    new Java2DImageProcessor().thumbnail(150, 150, resource(inputName), output, JPEG);
    BufferedImage thumbnail = ImageIO.read(output);
    assertNotNull(thumbnail);
    // The 640x361 source keeps its aspect ratio inside the 150x150 box.
    assertEquals(150, thumbnail.getWidth());
    assertTrue(thumbnail.getHeight() > 0 && thumbnail.getHeight() < 150);
  }

  @Test
  public void thumbnailsJpeg() throws Exception {
    assertThumbnailFits("a.jpg", "thumbnail.jpg");
  }

  @Test
  public void thumbnailsPngAsJpeg() throws Exception {
    assertThumbnailFits("a.png", "thumbnail.jpg");
  }
}
