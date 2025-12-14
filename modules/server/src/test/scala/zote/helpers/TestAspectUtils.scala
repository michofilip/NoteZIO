package zote.helpers

import zio.*
import zio.test.*
import zote.db.QuillContext

object TestAspectUtils {

  val rollback: TestAspect.PerTest[Nothing, QuillContext, Throwable, Throwable] =
    new TestAspect.PerTest {
      override def perTest[R >: Nothing <: QuillContext, E >: Throwable <: Throwable](
          test: ZIO[R, TestFailure[E], TestSuccess],
      )(implicit trace: Trace): ZIO[R, TestFailure[E], TestSuccess] = {
        val testResult = for {
          quillContext <- ZIO.service[QuillContext]
          testResult   <- quillContext.transaction {
            for {
              testResult <- test.either
              connection <- quillContext.dsDelegate.currentConnection.get
              _          <- ZIO.foreachDiscard(connection)(connection => ZIO.attemptBlocking(connection.rollback()))
            } yield testResult
          }
        } yield testResult

        testResult.catchAll { e => ZIO.left(TestFailure.fail(e)) }.absolve
      }
    }
}
