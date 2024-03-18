package io.github.tsnee.webgl.chapter2

import org.scalajs.dom
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext

object HelloPoint2 extends SimpleWebglProgram:
  override def vertexShaderSource: String = """
attribute vec4 a_Position;
void main() {
  gl_Position = a_Position;
  gl_PointSize = 10.0;
}
"""

  override def fragmentShaderSource: String = """
void main() {
  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
"""

  override def canvasId: String = "hello-point-2"

  override def run(gl: WebGLRenderingContext, program: WebGLProgram): Unit =
    val aPosition = gl.getAttribLocation(program, "a_Position")
    gl.vertexAttrib3f(aPosition, 0f, 0f, 0f)
    gl.clearColor(0f, 0f, 0f, 1f)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.useProgram(program)
    gl.drawArrays(WebGLRenderingContext.POINTS, 0, 1)
