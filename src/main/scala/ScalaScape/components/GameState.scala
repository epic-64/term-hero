package ScalaScape.components

class GameState(val targetFps: Int):
  var activityLog          = ActivityLog()
  var inventory            = Inventory()

  var skills: SkillList = new SkillList()
  val scenes: SceneList = new SceneList(this)
  var selectedScene: Scene = scenes.world

  var forceClearScreen: Boolean = false

  def getScene: Scene = selectedScene
  def swapScene(scene: Scene): GameState =
    selectedScene = scene
    activityLog.add(s"Entered ${scene.name}")(this)
    this.forceClearScreen = true // scene swapping causes lots of changes, so we want to clear the screen
    this
  end swapScene

  def update(): GameState = selectedScene.update(this)

  class SkillList {
    val woodcutting: Woodcutting = Woodcutting()
  }

  class SceneList(state: GameState):
    val world: WorldMenuScene = WorldMenuScene(state)
    val gathering: GatheringMenuScene = GatheringMenuScene(state)
    val woodcutting: WoodCuttingMenuScene = WoodCuttingMenuScene(state)
    val oak: OakScene = OakScene(state)
    val teak: TeakScene = TeakScene(state)
  end SceneList
end GameState