package io.github.tsnee.webgl.chapter2

import com.raquo.laminar.api.L._
import io.github.iltotore.iron._
import io.github.iltotore.iron.constraint.all._
import io.github.tsnee.webgl._
import io.github.tsnee.webgl.common.ContextExtractor
import io.github.tsnee.webgl.common.ProgramCreator
import io.github.tsnee.webgl.types._
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext

object HelloPoint1:
  def panel(height: Height, width: Width): Element =
    val canvas           = canvasTag(heightAttr := height, widthAttr := width)
    val successOrFailure =
      for
        gl <- ContextExtractor.extractWebglContext(canvas.ref)
        pg <- ProgramCreator.createProgram(gl, vertexShaderSource, fragmentShaderSource)
        _   = useWebgl(gl, pg)
      yield ()
    successOrFailure match
      case Right(())   => div(canvas)
      case Left(error) => div(error)

  val vertexShaderSource: VertexShaderSource = """
void main() {
  gl_Position = vec4(0.0, 0.0, 0.0, 1.0);
  gl_PointSize = 10.0;
}
"""

  val fragmentShaderSource: FragmentShaderSource = """
void main() {
  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
"""

  private def useWebgl(gl: WebGLRenderingContext, program: WebGLProgram): Unit =
    gl.clearColor(0f, 0f, 0f, 1f)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.useProgram(program)
    gl.drawArrays(WebGLRenderingContext.POINTS, 0, 1)
