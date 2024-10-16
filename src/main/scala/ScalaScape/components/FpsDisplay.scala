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

  def render(pos: Pos): TerminalParagraph =
    val frameTimeString = f"FrameTime: $frameTime%.1f ms"
    val realFrameTimeString = f"FPS (real): ${1_000 / frameTime}%.1f"
    
    TerminalParagraph(
      List(
        TerminalString("Frame Time", Pos(pos.x, pos.y), TextColor.ANSI.YELLOW),
        TerminalString("----------", Pos(pos.x, pos.y + 1), TextColor.ANSI.YELLOW),
        TerminalString(frameTimeString, Pos(pos.x, pos.y + 2), TextColor.ANSI.YELLOW),
        TerminalString(realFrameTimeString, Pos(pos.x, pos.y + 3), TextColor.ANSI.YELLOW),
        TerminalString(s"FPS (target): $targetFps", Pos(pos.x, pos.y + 4), TextColor.ANSI.YELLOW)
      )
    )
  end render
end FpsDisplay
