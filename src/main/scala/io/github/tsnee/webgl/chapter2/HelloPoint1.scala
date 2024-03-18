package io.github.tsnee.webgl.chapter2

import org.scalajs.dom
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext

object HelloPoint1 extends SimpleWebglProgram:
  override def vertexShaderSource: String = """
void main() {
  gl_Position = vec4(0.0, 0.0, 0.0, 1.0);
  gl_PointSize = 10.0;
}
"""

  override def fragmentShaderSource: String = """
void main() {
  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
"""

  override def canvasId: String = "hello-point-1"

  override def run(gl: WebGLRenderingContext, program: WebGLProgram): Unit =
    gl.clearColor(0f, 0f, 0f, 1f)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.useProgram(program)
    gl.drawArrays(WebGLRenderingContext.POINTS, 0, 1)
