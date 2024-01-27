package zote.utils

import zio.*

object ZIOUtils {
    extension [T](value: T) {
        def asZIO: UIO[T] = ZIO.succeed(value)
    }

    extension [R, E, T, K](zio: ZIO[R, E, Seq[T]]) {
        def toMap(key: T => K): ZIO[R, E, Map[K, T]] = {
            zio.map(_.map(x => key(x) -> x).toMap)
        }
    }

}
