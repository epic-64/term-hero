package ScalaScape.components

import com.googlecode.lanterna.TextColor

class FpsDisplay(targetFps: Int):
  private var frameTime: Double                    = 0.0
  private val fpsUpdateIntervalMs: Milliseconds    = 10
  private var timeSinceLastFpsUpdate: Milliseconds = 0
  private var lastEndTime: Milliseconds            = 0

  def update(elapsedTime: Milliseconds): Unit =
    timeSinceLastFpsUpdate += elapsedTime

    if timeSinceLastFpsUpdate >= fpsUpdateIntervalMs then
      frameTime = elapsedTime
      timeSinceLastFpsUpdate = 0
    end if
  end update

  def render(pos: Pos): RenderBlock =
    val frameTimeString = f"FrameTime: $frameTime%.1f ms"
    val realFrameTimeString = f"FPS (real): ${1_000 / frameTime}%.1f"
    val totalMemoryMB = Runtime.getRuntime.totalMemory() / 1_000_000
    val freeMemoryMB = Runtime.getRuntime.freeMemory() / 1_000_000

    RenderBlock(
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
