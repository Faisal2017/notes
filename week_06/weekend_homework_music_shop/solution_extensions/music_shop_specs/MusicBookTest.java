import static org.junit.Assert.*;
import org.junit.*;
import music_shop.*;
import behaviours.*;

public class MusicBookTest {

  MusicBook book;

  @Before
  public void before() {
    book = new MusicBook("The Six Chord Songbook", 3, 5);
  }

  @Test
  public void hasTitle() {
    assertEquals("The Six Chord Songbook", book.getTitle());
  }

  @Test
  public void hasBuyPrice() {
    assertEquals(3, book.getBuyPrice());
  }

  @Test
  public void hasSellPrice() {
    assertEquals(5, book.getSellPrice());
  }

  @Test
  public void canGetMarkUp() {
    assertEquals(2, book.calculateMarkup());
  }
}