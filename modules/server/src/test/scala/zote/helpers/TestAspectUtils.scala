package zote.helpers

import zio.*
import zio.test.*
import zote.db.QuillContext

object TestAspectUtils {
  private case class TransactionRollbackSuccessException(
      testSuccess: TestSuccess
  ) extends RuntimeException

  private case class TransactionRollbackFailureException[E](
      testFailure: TestFailure[E]
  ) extends RuntimeException

  val rollback
      : TestAspect.PerTest[Nothing, QuillContext, Throwable, Throwable] =
    new TestAspect.PerTest {
      override def perTest[
          R >: Nothing <: QuillContext,
          E >: Throwable <: Throwable
      ](
          test: ZIO[R, TestFailure[E], TestSuccess]
      )(implicit trace: Trace): ZIO[R, TestFailure[E], TestSuccess] = {
        for {
          quillContext <- ZIO.service[QuillContext]
          testResult <- quillContext
            .transaction {
              test.foldZIO(
                tf => ZIO.fail(TransactionRollbackFailureException(tf)),
                ts => ZIO.fail(TransactionRollbackSuccessException(ts))
              )
            }
            .flip
            .flatMap {
              case TransactionRollbackSuccessException(ts) => ZIO.succeed(ts)
              case TransactionRollbackFailureException(tf) =>
                ZIO.fail(tf.asInstanceOf[TestFailure[E]])
              case e => ZIO.fail(TestFailure.fail(e))
            }
        } yield testResult
      }
    }

//  val rollbackAll:TestAspect[Nothing, QuillContext, Nothing, Any]=new TestAspect{
//    override def some[R >: Nothing <: QuillContext, E >: Nothing <: Any](spec: Spec[R, E])(implicit trace: Trace): Spec[R, E] = {
//      for{
//        quillContext <- ZIO.service[QuillContext]
//        x<-quillContext.transaction{
//          spec.
//        }
//      }yield ???
//    }
//  }
}
