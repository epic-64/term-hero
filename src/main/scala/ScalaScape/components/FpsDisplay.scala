package ScalaScape.components

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.TextGraphics

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
  
  def render(position: Position): TerminalParagraph =
    TerminalParagraph(
      List(
        TerminalString("Frame Time", Position(position.x, position.y), TextColor.ANSI.YELLOW),
        TerminalString("----------", Position(position.x, position.y + 1), TextColor.ANSI.YELLOW),
        TerminalString(f"FrameTime: $frameTime%.1f ms", Position(position.x, position.y + 2), TextColor.ANSI.YELLOW),
        TerminalString(f"FPS (real): ${1_000 / frameTime}%.1f", Position(position.x, position.y + 3), TextColor.ANSI.YELLOW),
        TerminalString(s"FPS (target): $targetFps", Position(position.x, position.y + 4), TextColor.ANSI.YELLOW)
      )
    )
  end render
end FpsDisplay
