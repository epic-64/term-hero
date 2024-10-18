package ScalaScape.components

class GameState:
  val targetFps: Int             = 30
  var selectedScene: Scene       = WorldMenuScene()
  var inventory                  = new Inventory(
    Map(
      "Wood"  -> InventoryItem("Wood", 0),
      "Stone" -> InventoryItem("Stone", 0)
    )
  )
  var skills: Map[String, Skill] = Map(
    "Woodcutting" -> Woodcutting()
  )
end GameState
