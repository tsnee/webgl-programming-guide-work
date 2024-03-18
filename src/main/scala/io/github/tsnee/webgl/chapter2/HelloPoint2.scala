package io.github.tsnee.webgl.chapter2

import io.github.tsnee.webgl.WebglInitializer
import org.scalajs.dom
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.html.Canvas

object HelloPoint2:
  val vertexShaderSource: String = """
attribute vec4 a_Position;
void main() {
  gl_Position = a_Position;
  gl_PointSize = 10.0;
}
"""

  val fragmentShaderSource: String = """
void main() {
  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
"""

  def initialize(canvas: Canvas): Unit =
    WebglInitializer.initialize(
      canvas,
      vertexShaderSource,
      fragmentShaderSource,
      run
    )

  private def run(gl: WebGLRenderingContext, program: WebGLProgram): Unit =
    val aPosition = gl.getAttribLocation(program, "a_Position")
    gl.vertexAttrib3f(aPosition, 0f, 0f, 0f)
    gl.clearColor(0f, 0f, 0f, 1f)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.useProgram(program)
    gl.drawArrays(WebGLRenderingContext.POINTS, 0, 1)
