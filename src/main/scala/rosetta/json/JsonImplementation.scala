/* Copyright (C) 2011 by John A. De Goes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package rosetta.json

/** Represents the bundle of functionality necessary to work with a given
 * JSON library.
 */
trait JsonImplementation[Json] {
  // *************** BEGIN IMPLEMENTATION ***************
  implicit val BooleanSerializer: Serializer[Boolean]

  implicit val StringSerializer: Serializer[String]

  implicit val LongSerializer: Serializer[Long]

  implicit val DoubleSerializer: Serializer[Double]

  implicit def ObjectSerializer[A](implicit serializer: Serializer[A]): Serializer[Iterable[(String, A)]]

  implicit def ArraySerializer[A](implicit serializer: Serializer[A]): Serializer[Iterable[A]]

  implicit def OptionSerializer[A](implicit serializer: Serializer[A]): Serializer[Option[A]]

  def foldJson[Z](json: Json, matcher: JsonMatcher[Z]): Z

  // *************** END IMPLEMENTATION ***************
  implicit val JsonSerializer: Serializer[Json] = new Serializer[Json] {
    def serialize(v: Json): Json = v

    def deserialize(v: Json): Json = v
  }

  val EmptyObject = ObjectSerializer[Boolean].serialize(Nil)
  val EmptyArray  = ArraySerializer[Boolean].serialize(Nil)

  type JsonMatcher[Z] = (() => Z, Boolean => Z, Long => Z, Double => Z, String => Z, Iterable[Json] => Z, Iterable[(String, Json)] => Z)

  trait Serializer[A] {
    def serialize(v: A): Json

    def deserialize(v: Json): A
  }

  implicit final def anyToSerializable[A: Serializer](a: A) = new {
    def serialize: Json = implicitly[Serializer[A]].serialize(a)
  }

  implicit final def jsonToPimpedType(json: Json): PimpedJson = new PimpedJson(json)
  implicit final def pimpedTypeToJson(pimp: PimpedJson): Json = pimp.value

  val JsonNull = OptionSerializer[Boolean].serialize(None)

  object JsonBool {
    def apply(v: Boolean) = v.serialize

    def unapply(json: Json): Option[Boolean] = foldJson[Option[Boolean]](json,
      (
        () => None,
        v => Some(v),
        _ => None,
        _ => None,
        _ => None,
        _ => None,
        _ => None
      )
    )
  }

  object JsonLong {
    def apply(v: Long): Json = v.serialize

    def unapply(json: Json): Option[Long] = foldJson[Option[Long]](json,
      (
        () => None,
        _ => None,
        v => Some(v),
        _ => None,
        _ => None,
        _ => None,
        _ => None
      )
    )
  }

  object JsonDouble {
    def apply(v: Double): Json = v.serialize

    def unapply(json: Json): Option[Double] = foldJson[Option[Double]](json,
      (
        () => None,
        _ => None,
        _ => None,
        v => Some(v),
        _ => None,
        _ => None,
        _ => None
      )
    )
  }

  object JsonString {
    def apply(v: String): Json = v.serialize

    def unapply(json: Json): Option[String] = foldJson[Option[String]](json,
      (
        () => None,
        _ => None,
        _ => None,
        _ => None,
        v => Some(v),
        _ => None,
        _ => None
      )
    )
  }

  object JsonArray {
    def apply(v: Iterable[Json]): Json = v.serialize

    def unapply(json: Json): Option[List[Json]] = foldJson[Option[List[Json]]](json,
      (
        () => None,
        _ => None,
        _ => None,
        _ => None,
        _ => None,
        v => Some(v.toList),
        _ => None
      )
    )
  }

  object JsonObject {
    def apply(v: Iterable[(String, Json)]): Json = v.serialize

    def unapply(json: Json): Option[Map[String, Json]] = foldJson[Option[Map[String, Json]]](json,
      (
        () => None,
        _ => None,
        _ => None,
        _ => None,
        _ => None,
        _ => None,
        v => Some(Map.empty ++ v)
      )
    )
  }

  case class PimpedJson(value: Json) {
    def deserialize[A: Serializer]: A = implicitly[Serializer[A]].deserialize(value)

    def fold[Z](ifNull: => Z, ifBool: Boolean => Z, ifLong: Long => Z, ifDouble: Double => Z, ifString: String => Z, ifArray: Iterable[Json] => Z, ifObject: Iterable[(String, Json)] => Z): Z = {
      foldJson[Z](
        json    = value,
        matcher = (() => ifNull, ifBool, ifLong, ifDouble, ifString, ifArray, ifObject)
      )
    }

    def mapDown(f: Json => Json): Json = foldJson(value, FoldMapDown(f))

    def mapUp(f: Json => Json): Json = foldJson(value, FoldMapUp(f))

    def foldUp[Z](state: Z, f: (Z, Json) => Z): Z = foldJson(value, FoldFoldUp(state, f))

    def foldDown[Z](state: Z, f: (Z, Json) => Z): Z = foldJson(value, FoldFoldDown(state, f))

    def get(name: String): Json = value match {
      case JsonObject(map) =>
        name.split("\\.").toList.filter(_.length > 0) match {
          case Nil => value

          case f :: fs =>
            map.get(f) match {
              case None        => JsonNull
              case Some(value) => value.get(fs.mkString("."))
            }
        }

      case _ => JsonNull
    }

    override def toString = value.toString
  }

  private def FoldMapDown(f: Json => Json): JsonMatcher[Json] = (
    () => f(JsonNull),
    v => f(v.serialize),
    v => f(v.serialize),
    v => f(v.serialize),
    v => f(v.serialize),
    v => {
      val as = ArraySerializer(JsonSerializer)

      val mapped = as.deserialize(f(as.serialize(v)))

      as.serialize(mapped.map { element =>
        foldJson[Json](element, FoldMapDown(f))
      })
    },
    v => {
      val os = ObjectSerializer(JsonSerializer)

      val mapped = os.deserialize(f(os.serialize(v)))

      os.serialize(mapped.map { tuple =>
        val (name, value) = tuple

        (name, foldJson[Json](value, FoldMapDown(f)))
      })
    }
  )

  private def FoldMapUp(f: Json => Json): JsonMatcher[Json] = (
    () => f(JsonNull),
    v => f(v.serialize),
    v => f(v.serialize),
    v => f(v.serialize),
    v => f(v.serialize),
    v => {
      val as = ArraySerializer(JsonSerializer)

      f(as.serialize(v.map { element =>
        foldJson[Json](element, FoldMapUp(f))
      }))
    },
    v => {
      val os = ObjectSerializer(JsonSerializer)

      f(os.serialize(v.map { tuple =>
        val (name, value) = tuple

        (name, foldJson[Json](value, FoldMapUp(f)))
      }))
    }
  )

  private def FoldFoldDown[Z](state: Z, f: (Z, Json) => Z): JsonMatcher[Z] = (
    () => f(state, JsonNull),
    v => f(state, v.serialize),
    v => f(state, v.serialize),
    v => f(state, v.serialize),
    v => f(state, v.serialize),
    v => {
      val as = ArraySerializer(JsonSerializer)

      val newState = f(state, as.serialize(v))

      v.foldLeft(newState) { (state, element) =>
        foldJson[Z](element, FoldFoldDown[Z](state, f))
      }
    },
    v => {
      val os = ObjectSerializer(JsonSerializer)

      val newState = f(state, os.serialize(v))

      v.foldLeft(newState) { (state, field) =>
        val (name, value) = field

        foldJson[Z](value, FoldFoldDown[Z](state, f))
      }
    }
  )

  private def FoldFoldUp[Z](state: Z, f: (Z, Json) => Z): JsonMatcher[Z] = (
    () => f(state, JsonNull),
    v => f(state, v.serialize),
    v => f(state, v.serialize),
    v => f(state, v.serialize),
    v => f(state, v.serialize),
    v => {
      val as = ArraySerializer(JsonSerializer)

      val newState = v.foldLeft(state) { (state, element) =>
        foldJson[Z](element, FoldFoldUp[Z](state, f))
      }

      f(newState, as.serialize(v))
    },
    v => {
      val os = ObjectSerializer(JsonSerializer)

      val newState = v.foldLeft(state) { (state, field) =>
        val (name, value) = field

        foldJson[Z](value, FoldFoldUp[Z](state, f))
      }

      f(newState, os.serialize(v))
    }
  )
}