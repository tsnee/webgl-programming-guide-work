package io.github.tsnee.webgl

import cats._
import cats.syntax.all._
import com.raquo.laminar.api.L._
import io.github.iltotore.iron._
import io.github.tsnee.webgl.types._
import org.scalajs.dom
import org.scalajs.dom.MouseEvent

import scala.annotation.unused
import scala.collection.SortedSet

object LaminarApp:
  val CanvasHeight: Height           = 400
  val CanvasWidth: Width             = 400
  // nav elements z-index is MaxZindex - chapter#, so lower-numbered chapters appear over higher ones
  val MaxZindex: Int                 = 12
  val NavPanelSelector: String       = "nav"
  val ExercisePanelSelector: String  = "div#scala-js"
  val AnswerKeyPanelSelector: String = "div#js"
  val AnswerKeyBaseUrl               = "https://rodger.global-linguist.com/webgl"

  private val appDescriptor = SortedSet(
    ExerciseDescriptor("HelloCanvas", 2, 0, 450, 450, chapter2.HelloCanvas.panel),
    ExerciseDescriptor("HelloPoint1", 2, 1, 450, 450, chapter2.HelloPoint1.panel),
    ExerciseDescriptor("HelloPoint2", 2, 2, 450, 450, chapter2.HelloPoint2.panel),
    ExerciseDescriptor("ClickedPoints", 2, 3, 450, 450, chapter2.ClickedPoints.panel),
    ExerciseDescriptor("ColoredPoints", 2, 4, 450, 450, chapter2.ColoredPoints.panel),
    ExerciseDescriptor("MultiPoint", 3, 0, 450, 450, chapter3.MultiPoint.panel),
    ExerciseDescriptor("HelloTriangle", 3, 1, 450, 450, chapter3.HelloTriangle.panel),
    ExerciseDescriptor("HelloTriangle_LINES", 3, 2, 450, 450, chapter3.HelloTriangleLines.panel),
    ExerciseDescriptor("HelloTriangle_LINE_STRIP", 3, 3, 450, 450, chapter3.HelloTriangleLineStrip.panel),
    ExerciseDescriptor("HelloTriangle_LINE_LOOP", 3, 4, 450, 450, chapter3.HelloTriangleLineLoop.panel),
    ExerciseDescriptor("HelloQuad", 3, 5, 450, 450, chapter3.HelloQuad.panel),
    ExerciseDescriptor("HelloQuad_FAN", 3, 6, 450, 450, chapter3.HelloQuadFan.panel),
    ExerciseDescriptor("TranslatedTriangle", 3, 7, 450, 450, chapter3.TranslatedTriangle.panel),
    ExerciseDescriptor("RotatedTriangle", 3, 8, 450, 450, chapter3.RotatedTriangle.panel),
    ExerciseDescriptor("RotatedTriangle_Matrix", 3, 9, 450, 450, chapter3.RotatedTriangleMatrix.panel),
    ExerciseDescriptor("ScaledTriangle_Matrix", 3, 10, 450, 450, chapter3.ScaledTriangleMatrix.panel),
    ExerciseDescriptor("RotatedTriangle_Matrix4", 4, 0, 450, 450, chapter4.RotatedTriangleMatrix4.panel),
    ExerciseDescriptor("RotatedTranslatedTriangle", 4, 1, 450, 450, chapter4.RotatedTranslatedTriangle.panel),
    ExerciseDescriptor("RotatingTriangle", 4, 2, 450, 450, chapter4.RotatingTriangle.panel),
    ExerciseDescriptor("RotatingTranslatedTriangle", 4, 3, 450, 450, chapter4.RotatingTranslatedTriangle.panel),
    ExerciseDescriptor("MultiAttributeSize", 5, 0, 450, 450, chapter5.MultiAttributeSize.panel),
    ExerciseDescriptor("MultiAttributeSize_Interleaved", 5, 1, 450, 450, chapter5.MultiAttributeSizeInterleaved.panel),
    ExerciseDescriptor("MultiAttributeColor", 5, 2, 450, 450, chapter5.MultiAttributeColor.panel),
    ExerciseDescriptor("ColoredTriangle", 5, 3, 450, 450, chapter5.ColoredTriangle.panel),
    ExerciseDescriptor("HelloTriangle_FragCoord", 5, 4, 450, 450, chapter5.HelloTriangleFragCoord.panel),
    ExerciseDescriptor("TexturedQuad", 5, 5, 450, 450, chapter5.TexturedQuad.panel),
    ExerciseDescriptor("TexturedQuad_Repeat", 5, 6, 450, 450, chapter5.TexturedQuadRepeat.panel),
    ExerciseDescriptor("TexturedQuad_Clamp_Mirror", 5, 7, 450, 450, chapter5.TexturedQuadClampMirror.panel),
    ExerciseDescriptor("MultiTexture", 5, 8, 450, 450, chapter5.MultiTexture.panel),
    ExerciseDescriptor("LookAtTriangles", 7, 0, 450, 450, chapter7.LookAtTriangles.panel),
    ExerciseDescriptor("LookAtRotatedTriangles", 7, 1, 450, 450, chapter7.LookAtRotatedTriangles.panel),
    ExerciseDescriptor(
      "LookAtRotatedTriangles_mvMatrix",
      7,
      2,
      400,
      400,
      chapter7.LookAtRotatedTrianglesMvMatrix.panel
    ),
    ExerciseDescriptor("LookAtTrianglesWithKeys", 7, 3, 450, 450, chapter7.LookAtTrianglesWithKeys.panel),
    ExerciseDescriptor("OrthoView", 7, 4, 500, 450, chapter7.OrthoView.panel),
    ExerciseDescriptor(
      "LookAtTrianglesWithKeys_ViewVolume",
      7,
      5,
      450,
      450,
      chapter7.LookAtTrianglesWithKeysViewVolume.panel
    ),
    ExerciseDescriptor("OrthoView_halfSize", 7, 6, 500, 450, chapter7.OrthoViewHalfSize.panel),
    ExerciseDescriptor("OrthoView_halfWidth", 7, 7, 500, 450, chapter7.OrthoViewHalfWidth.panel),
    ExerciseDescriptor("PerspectiveView", 7, 8, 450, 450, chapter7.PerspectiveView.panel),
    ExerciseDescriptor("PerspectiveView_mvp", 7, 9, 450, 450, chapter7.PerspectiveViewMvp.panel),
    ExerciseDescriptor("PerspectiveView_mvpMatrix", 7, 10, 450, 450, chapter7.PerspectiveViewMvpMatrix.panel),
    ExerciseDescriptor("DepthBuffer", 7, 11, 450, 450, chapter7.DepthBuffer.panel),
    ExerciseDescriptor("Zfighting", 7, 12, 450, 450, chapter7.Zfighting.panel),
    ExerciseDescriptor("HelloCube", 7, 13, 450, 450, chapter7.HelloCube.panel),
    ExerciseDescriptor("ColoredCube", 7, 14, 450, 450, chapter7.ColoredCube.panel),
    ExerciseDescriptor("ColoredCube_singleColor", 7, 15, 450, 450, chapter7.ColoredCubeSingleColor.panel),
    ExerciseDescriptor("LightedCube", 8, 0, 450, 450, chapter8.LightedCube.panel),
    ExerciseDescriptor("LightedCube_ambient", 8, 1, 450, 450, chapter8.LightedCubeAmbient.panel),
    ExerciseDescriptor("LightedTranslatedRotatedCube", 8, 2, 450, 450, chapter8.LightedTranslatedRotatedCube.panel),
    ExerciseDescriptor("PointLightedCube", 8, 3, 450, 450, chapter8.PointLightedCube.panel),
    ExerciseDescriptor("PointLightedCube_perFragment", 8, 4, 450, 450, chapter8.PointLightedCubePerFragment.panel),
    ExerciseDescriptor("JointModel", 9, 0, 525, 450, chapter9.JointModel.panel),
    ExerciseDescriptor("MultiJointModel", 9, 1, 550, 450, chapter9.MultiJointModel.panel),
    ExerciseDescriptor("MultiJointModel_segment", 9, 2, 550, 450, chapter9.MultiJointModelSegment.panel),
    ExerciseDescriptor("RotateObject", 10, 0, 450, 450, chapter10.RotateObject.panel),
    ExerciseDescriptor("PickObject", 10, 1, 450, 450, chapter10.PickObject.panel),
    ExerciseDescriptor("PickFace", 10, 2, 450, 450, chapter10.PickFace.panel),
    ExerciseDescriptor("HUD", 10, 3, 450, 450, chapter10.Hud.panel),
    ExerciseDescriptor("3DoverWeb", 10, 4, 450, 450, chapter10.ThreeDOverWeb.panel),
    ExerciseDescriptor("Fog", 10, 5, 500, 450, chapter10.Fog.panel),
    ExerciseDescriptor("Fog_w", 10, 6, 450, 450, chapter10.FogW.panel),
    ExerciseDescriptor("RoundedPoints", 10, 7, 450, 450, chapter10.RoundedPoints.panel),
    ExerciseDescriptor("LookAtBlendedTriangles", 10, 8, 450, 450, chapter10.LookAtBlendedTriangles.panel),
    ExerciseDescriptor("BlendedCube", 10, 9, 450, 450, chapter10.BlendedCube.panel),
    ExerciseDescriptor("ProgramObject", 10, 10, 450, 450, chapter10.ProgramObject.panel),
    ExerciseDescriptor("FramebufferObject", 10, 11, 450, 450, chapter10.FramebufferObject.panel),
    ExerciseDescriptor("Shadow", 10, 12, 450, 450, chapter10.Shadow.panel),
    ExerciseDescriptor("Shadow_highp", 10, 13, 450, 450, chapter10.ShadowHighP.panel)
  )

  @main
  def run(): Unit =
    (
      Option(dom.document.querySelector(NavPanelSelector)),
      Option(dom.document.querySelector(ExercisePanelSelector)),
      Option(dom.document.querySelector(AnswerKeyPanelSelector))
    ) match
      case (Some(navElement), Some(exercisePanel), Some(answerKeyPanel)) =>
        val _ = render(navElement, buildNavPanel(appDescriptor, exercisePanel, answerKeyPanel))
      case _                                                             =>
        val errorMessage = buildErrorMessage("This HTML document doesn't have the right elements.")
        val _            = dom.document.body.appendChild(errorMessage.ref)

  private def buildNavPanel(
      appDescriptor: SortedSet[ExerciseDescriptor],
      exercisePanel: dom.Element,
      answerKeyPanel: dom.Element
  ) =
    val chapterLis = appDescriptor
      .groupBy(_.chapter)
      .toList
      .sortBy(_._1)
      .map(buildChapterListItem(exercisePanel, answerKeyPanel))
    ul(chapterLis)

  private def buildChapterListItem(
      exercisePanel: dom.Element,
      answerKeyPanel: dom.Element
  )(
      chapter: Chapter,
      exercises: SortedSet[ExerciseDescriptor]
  ) =
    val chapterTitle = span(s"Chapter $chapter")
    val exerciseLis  = exercises.toList.map(buildExerciseListItem(exercisePanel, answerKeyPanel))
    li(cls := "parent", zIndex := MaxZindex - chapter, chapterTitle, ul(cls := "child", exerciseLis))

  private def buildExerciseListItem(
      exercisePanel: dom.Element,
      answerKeyPanel: dom.Element
  )(
      desc: ExerciseDescriptor
  ) =
    li(
      span(desc.label),
      onClick --> handleClick(exercisePanel, answerKeyPanel, desc)
    )

  private val mountedExercise  = Var[Option[RootNode]](None)
  private val mountedAnswerKey = Var[Option[RootNode]](None)

  private def handleClick(
      exerciseContainer: dom.Element,
      answerKeyContainer: dom.Element,
      desc: ExerciseDescriptor
  )(
      @unused evt: MouseEvent
  ): Unit =
    val newExercisePanel  = desc.panel(CanvasWidth, CanvasHeight)
    unmount(mountedExercise.now())
    val newExercise       = render(exerciseContainer, newExercisePanel)
    mountedExercise.set(Some(newExercise))
    val newAnswerKeyPanel =
      iframe(
        src        := answerKeySrc(desc),
        heightAttr := desc.answerKeyHeight,
        widthAttr  := desc.answerKeyWidth
      )
    unmount(mountedAnswerKey.now())
    val newAnswerKey      = render(answerKeyContainer, newAnswerKeyPanel)
    mountedAnswerKey.set(Some(newAnswerKey))

  private def unmount(nodeOpt: Option[RootNode]): Unit =
    nodeOpt.foreach(_.unmount())

  private def answerKeySrc(desc: ExerciseDescriptor): String =
    val dirname  = f"ch${desc.chapter}%02d" // left-pad with at most one '0' e.g. "ch05"
    val filename = s"${desc.label}.html"
    s"$AnswerKeyBaseUrl/$dirname/$filename"

  private def buildErrorMessage(message: String) =
    h1(message)
