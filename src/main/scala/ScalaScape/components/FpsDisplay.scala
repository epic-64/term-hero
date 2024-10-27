package ScalaScape.components

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.input.{KeyStroke, KeyType}

class FpsDisplay(targetFps: Int):
  private val updateInterval: Milliseconds      = 50.0
  private var frameTime: Milliseconds           = 0.0
  private var timeSinceLastUpdate: Milliseconds = 0.0
  private var isVisible: Boolean                = true

  def update(elapsedTime: Milliseconds): FpsDisplay =
    if !isVisible then return this

    timeSinceLastUpdate += elapsedTime

    if (timeSinceLastUpdate >= updateInterval) then
      frameTime = elapsedTime
      timeSinceLastUpdate = 0
    end if

    this
  end update

  def handleInput(key: KeyStroke): FpsDisplay =
    key.getKeyType match
      case KeyType.F4 =>
        isVisible = !isVisible
        this
      case _          => this
  end handleInput

  def render(pos: Pos): RenderedBlock =
    if !isVisible then return RenderedBlock.empty

    val frameTimeString     = f"FrameTime: $frameTime%.1f ms"
    val realFrameTimeString = f"FPS (pot.): ${1_000 / frameTime}%.1f"
    val totalMemoryMB       = Runtime.getRuntime.totalMemory() / 1_000_000
    val freeMemoryMB        = Runtime.getRuntime.freeMemory() / 1_000_000

    RenderedBlock(
      List(
        RenderString("Frame Time", Pos(pos.x, pos.y), TextColor.ANSI.YELLOW),
        RenderString("----------", Pos(pos.x, pos.y + 1), TextColor.ANSI.YELLOW),
        RenderString(frameTimeString, Pos(pos.x, pos.y + 2), TextColor.ANSI.YELLOW),
        RenderString(realFrameTimeString, Pos(pos.x, pos.y + 3), TextColor.ANSI.YELLOW),
        RenderString(s"FPS (target): $targetFps", Pos(pos.x, pos.y + 4), TextColor.ANSI.YELLOW),
        RenderString(s"Total RAM MB: $totalMemoryMB", Pos(pos.x, pos.y + 5), TextColor.ANSI.YELLOW),
        RenderString(s"Free RAM MB: $freeMemoryMB", Pos(pos.x, pos.y + 6), TextColor.ANSI.YELLOW)
      )
    )
  end render
end FpsDisplay
