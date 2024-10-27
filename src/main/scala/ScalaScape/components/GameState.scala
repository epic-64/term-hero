package ScalaScape.components

class GameState(val targetFps: Int):
  val eventLog  = EventLog(capacity = 20)
  val inventory = Inventory()
  val skills    = SkillList()
  val scenes    = SceneList(this)

  var houseLevel = 0

  private var selectedScene: Scene = scenes.world
  var forceClearScreen: Boolean    = false

  def update(): GameState                = selectedScene.update(this)
  def getScene: Scene                    = selectedScene
  def swapScene(scene: Scene): GameState =
    selectedScene = scene
    eventLog.add(s"Entered ${scene.name}")(this)
    this.forceClearScreen = true // scene swapping causes lots of changes, so we want to clear the screen
    this
  end swapScene

  class SkillList {
    val woodcutting: Woodcutting = Woodcutting()
  }

  class SceneList(state: GameState):
    val world: WorldMenuScene             = WorldMenuScene(state)
    val gathering: GatheringMenuScene     = GatheringMenuScene(state)
    val woodcutting: WoodCuttingMenuScene = WoodCuttingMenuScene(state)
    val oak: OakScene                     = OakScene(state)
    val teak: TeakScene                   = TeakScene(state)
    val building: BuildingMenuScene       = BuildingMenuScene(state)
    val houseBuilding: HouseBuildingScene = HouseBuildingScene(state)
  end SceneList
end GameState
