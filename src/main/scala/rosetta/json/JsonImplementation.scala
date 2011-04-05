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

import rosetta.io.{Serializer, SerializerImplicits}

/** Represents the bundle of functionality necessary to work with a given
 * JSON library.
 */
trait JsonImplementation[Json] extends SerializerImplicits {
  type JsonSerializer[A] = Serializer[A, Json]

  // *************** BEGIN IMPLEMENTATION ***************
  implicit def JsonToString: Serializer[Json, String]

  implicit def BooleanToJson: JsonSerializer[Boolean]

  implicit def StringToJson: JsonSerializer[String]

  implicit def LongToJson: JsonSerializer[Long]

  implicit def DoubleToJson: JsonSerializer[Double]

  implicit def ObjectToJson[A](implicit serializer: JsonSerializer[A]): JsonSerializer[Iterable[(String, A)]]

  implicit def ArrayToJson[A](implicit serializer: JsonSerializer[A]): JsonSerializer[Iterable[A]]

  implicit def OptionToJson[A](implicit serializer: JsonSerializer[A]): JsonSerializer[Option[A]]

  def foldJson[Z](json: Json, matcher: JsonMatcher[Z]): Z

  // *************** END IMPLEMENTATION ***************
  def JsonToJson: JsonSerializer[Json] = implicitly[JsonSerializer[Json]]

  implicit def ObjectAsMapToJson[A](implicit serializer: JsonSerializer[A]): JsonSerializer[Map[String, A]] = new JsonSerializer[Map[String, A]] {
    def serialize(v: Map[String, A]): Json = ObjectToJson[A].serialize(v)

    def deserialize(v: Json): Map[String, A] = ObjectToJson[A].deserialize(v).toMap
  }

  implicit def ArrayAsListToJson[A](implicit serializer: JsonSerializer[A]): JsonSerializer[List[A]] = new JsonSerializer[List[A]] {
    def serialize(v: List[A]): Json = ArrayToJson[A].serialize(v)

    def deserialize(v: Json): List[A] = ArrayToJson[A].deserialize(v).toList
  }

  lazy val EmptyObject = (Map.empty[String, String]: Iterable[(String, String)]).serialize[Json]
  lazy val EmptyArray  = (Nil: Iterable[String]).serialize[Json]

  type JsonMatcher[Z] = (() => Z, Boolean => Z, Long => Z, Double => Z, String => Z, Iterable[Json] => Z, Iterable[(String, Json)] => Z)

  implicit final def jsonToPimpedType(json: Json): PimpedJson = new PimpedJson(json)
  implicit final def pimpedTypeToJson(pimp: PimpedJson): Json = pimp.value

  lazy val JsonNull: Json = (None: Option[Boolean]).serialize[Json]

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
    def apply(v: Iterable[Json]): Json = v.serialize[Json]

    def unapply(json: Json): Option[Iterable[Json]] = foldJson[Option[Iterable[Json]]](json,
      (
        () => None,
        _ => None,
        _ => None,
        _ => None,
        _ => None,
        v => Some(v),
        _ => None
      )
    )
  }

  object JsonObject {
    def apply(v: Iterable[(String, Json)]): Json = v.serialize[Json]

    def unapply(json: Json): Option[Iterable[(String, Json)]] = foldJson[Option[Iterable[(String, Json)]]](json,
      (
        () => None,
        _ => None,
        _ => None,
        _ => None,
        _ => None,
        _ => None,
        v => Some(v)
      )
    )
  }

  case class PimpedJson(value: Json) {
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
            map.find(_._1 == f) match {
              case None             => JsonNull
              case Some((_, value)) => value.get(fs.mkString("."))
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
      val as = ArrayToJson(JsonToJson)

      val mapped = as.deserialize(f(as.serialize(v)))

      as.serialize(mapped.map { element =>
        foldJson[Json](element, FoldMapDown(f))
      })
    },
    v => {
      val os = ObjectToJson(JsonToJson)

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
      val as = ArrayToJson(JsonToJson)

      f(as.serialize(v.map { element =>
        foldJson[Json](element, FoldMapUp(f))
      }))
    },
    v => {
      val os = ObjectToJson(JsonToJson)

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
      val as = ArrayToJson(JsonToJson)

      val newState = f(state, as.serialize(v))

      v.foldLeft(newState) { (state, element) =>
        foldJson[Z](element, FoldFoldDown[Z](state, f))
      }
    },
    v => {
      val os = ObjectToJson(JsonToJson)

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
      val as = ArrayToJson(JsonToJson)

      val newState = v.foldLeft(state) { (state, element) =>
        foldJson[Z](element, FoldFoldUp[Z](state, f))
      }

      f(newState, as.serialize(v))
    },
    v => {
      val os = ObjectToJson(JsonToJson)

      val newState = v.foldLeft(state) { (state, field) =>
        val (name, value) = field

        foldJson[Z](value, FoldFoldUp[Z](state, f))
      }

      f(newState, os.serialize(v))
    }
  )
}