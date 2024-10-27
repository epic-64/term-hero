import ScalaScape.utils.AsList
import org.scalatest.funsuite.AnyFunSuite

case class TestItem(name: String, quantity: Int)

class AsListTest extends AnyFunSuite {
  test("asList contains expected elements") {
    class TestItemList extends AsList {
      val item1: TestItem = TestItem("Item 1", 1)
      val item3: TestItem = TestItem("Item 3", 3)
      val item2: TestItem = TestItem("Item 2", 2)
    }

    val testItemList = new TestItemList()
    val items = testItemList.asList[TestItem].sortBy(_.quantity)

    assert(items == List(
      TestItem("Item 1", 1),
      TestItem("Item 2", 2),
      TestItem("Item 3", 3),
    ))
  }
}
