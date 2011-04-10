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

  implicit def MapToJson[A](implicit serializer: JsonSerializer[A]): JsonSerializer[Map[String, A]] = new JsonSerializer[Map[String, A]] {
    def serialize(v: Map[String, A]): Json = ObjectToJson[A].serialize(v)

    def deserialize(v: Json): Map[String, A] = ObjectToJson[A].deserialize(v).toMap
  }

  implicit def ListToJson[A](implicit serializer: JsonSerializer[A]): JsonSerializer[List[A]] = new JsonSerializer[List[A]] {
    def serialize(v: List[A]): Json = ArrayToJson[A].serialize(v)

    def deserialize(v: Json): List[A] = ArrayToJson[A].deserialize(v).toList
  }

  implicit def SetToJson[A](implicit serializer: JsonSerializer[A]): JsonSerializer[Set[A]] = new JsonSerializer[Set[A]] {
    def serialize(v: Set[A]): Json = ArrayToJson[A].serialize(v)

    def deserialize(v: Json): Set[A] = ArrayToJson[A].deserialize(v).toSet
  }

  implicit lazy val IntToJson = new JsonSerializer[Int] {
    def serialize(v: Int): Json = LongToJson.serialize(v.toLong)

    def deserialize(v: Json): Int = LongToJson.deserialize(v).toInt
  }

  implicit lazy val FloatToJson = new JsonSerializer[Float] {
    def serialize(v: Float): Json = DoubleToJson.serialize(v.toDouble)

    def deserialize(v: Json): Float = DoubleToJson.deserialize(v).toFloat
  }

  implicit def Tuple2ToJson[A: JsonSerializer, B: JsonSerializer] = new JsonSerializer[(A, B)] {
    implicit val aSI = implicitly[JsonSerializer[A]].inverse
    implicit val bSI = implicitly[JsonSerializer[B]].inverse

    def serialize(v: (A, B)): Json = (v._1.serialize[Json] :: v._2.serialize[Json] :: Nil).serialize[Json]

    def deserialize(v: Json): (A, B) = v.deserialize[List[Json]] match {
      case a :: b :: Nil => (a.deserialize[A], b.serialize[B])

      case _ => error("Expected Tuple2 (array with 2 elements) but found: " + v)
    }
  }

  import java.util.{Date => JDate}
  implicit val DateToJson = new JsonSerializer[JDate] {
    def serialize(v: JDate): Json = v.getTime.serialize

    def deserialize(v: Json): JDate = new JDate(v.deserialize[Long])
  }

  implicit def Tuple3ToJson[A: JsonSerializer, B: JsonSerializer, C: JsonSerializer] = new JsonSerializer[(A, B, C)] {
    implicit val aSI = implicitly[JsonSerializer[A]].inverse
    implicit val bSI = implicitly[JsonSerializer[B]].inverse
    implicit val cSI = implicitly[JsonSerializer[C]].inverse

    def serialize(v: (A, B, C)): Json = {
      (v._1.serialize[Json] ::
       v._2.serialize[Json] ::
       v._3.serialize[Json] :: Nil).serialize[Json]
    }

    def deserialize(v: Json): (A, B, C) = v.deserialize[List[Json]] match {
      case a :: b :: c :: Nil => (a.deserialize[A], b.deserialize[B], c.deserialize[C])

      case _ => error("Expected Tuple3 (array with 3 elements) but found: " + v)
    }
  }

  implicit def Tuple4ToJson[A: JsonSerializer, B: JsonSerializer, C: JsonSerializer, D: JsonSerializer] = new JsonSerializer[(A, B, C, D)] {
    implicit val aSI = implicitly[JsonSerializer[A]].inverse
    implicit val bSI = implicitly[JsonSerializer[B]].inverse
    implicit val cSI = implicitly[JsonSerializer[C]].inverse
    implicit val dSI = implicitly[JsonSerializer[D]].inverse

    def serialize(v: (A, B, C, D)): Json = {
      (v._1.serialize[Json] ::
       v._2.serialize[Json] ::
       v._3.serialize[Json] ::
       v._4.serialize[Json] :: Nil).serialize[Json]
    }

    def deserialize(v: Json): (A, B, C, D) = v.deserialize[List[Json]] match {
      case a :: b :: c :: d :: Nil => (a.deserialize[A], b.deserialize[B], c.deserialize[C], d.deserialize[D])

      case _ => error("Expected Tuple4 (array with 4 elements) but found: " + v)
    }
  }

  implicit def Tuple5ToJson[A: JsonSerializer, B: JsonSerializer, C: JsonSerializer, D: JsonSerializer, E: JsonSerializer] = new JsonSerializer[(A, B, C, D, E)] {
    implicit val aSI = implicitly[JsonSerializer[A]].inverse
    implicit val bSI = implicitly[JsonSerializer[B]].inverse
    implicit val cSI = implicitly[JsonSerializer[C]].inverse
    implicit val dSI = implicitly[JsonSerializer[D]].inverse
    implicit val eSI = implicitly[JsonSerializer[E]].inverse

    def serialize(v: (A, B, C, D, E)): Json = {
      (v._1.serialize[Json] ::
       v._2.serialize[Json] ::
       v._3.serialize[Json] ::
       v._4.serialize[Json] ::
       v._5.serialize[Json] :: Nil).serialize[Json]
    }

    def deserialize(v: Json): (A, B, C, D, E) = v.deserialize[List[Json]] match {
      case a :: b :: c :: d :: e :: Nil => (a.deserialize[A], b.deserialize[B], c.deserialize[C], d.deserialize[D], e.deserialize[E])

      case _ => error("Expected Tuple5 (array with 5 elements) but found: " + v)
    }
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
    def apply(v: (String, Json)*): Json = (v.toIterable).serialize[Json]

    def apply(v: Iterable[(String, Json)]): Json = v.serialize[Json]

    def unapply(json: Json): Option[Map[String, Json]] = foldJson[Option[Map[String, Json]]](json,
      (
        () => None,
        _ => None,
        _ => None,
        _ => None,
        _ => None,
        _ => None,
        v => Some(v.toMap)
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

    /** Retrieves the specified field of this object. If this is not an object,
     * or if there is no such field, then it returns the Json representation
     * of null.
     *
     * {{{
     * json.get(".foo.baz")
     * }}}
     */
    def get(name: String): Json = name.split("[.]").toList.filter(_.length > 0) match {
      case Nil => value

      case f :: fs =>
        value match {
          case JsonObject(map) =>
            map.get(name) match {
              case None        => JsonNull
              case Some(value) => value.get(fs.mkString("."))
            }

          case _ => JsonNull
        }
    }

    /** Concatenation monoid. Wraps either side in an array unless either
     * (a) one side is an array, in which case the other side is added to the
     * array, or (b) both sides are arrays, in which case the arrays are
     * concatenated together.
     */
    def ++ (that: Json): Json = foldJson(value, FoldConcat(that))

    /** Merges this json object with the specified json object. This is mainly
     * used to take two json objects and merge their fields recursively.
     *
     * Merging a json value with an array adds the value to the array.
     *
     * Merging two primitives returns the second primitive.
     */
    def merge(that: Json): Json = foldJson(value, FoldMerge(that))

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

  private def FoldConcat(that: Json): JsonMatcher[Json] = (
    ()    => that,
    bool1 => foldJson[Json](that, (
      ()      => that,
      bool2   => (bool1.serialize[Json] :: bool2.serialize[Json]   :: Nil).serialize[Json],
      long2   => (bool1.serialize[Json] :: long2.serialize[Json]   :: Nil).serialize[Json],
      double2 => (bool1.serialize[Json] :: double2.serialize[Json] :: Nil).serialize[Json],
      string2 => (bool1.serialize[Json] :: string2.serialize[Json] :: Nil).serialize[Json],
      array2  => (bool1.serialize[Json] :: array2.toList).serialize[Json],
      object2 => (bool1.serialize[Json] :: object2.serialize[Json] :: Nil).serialize[Json]
    )),
    long1   => foldJson[Json](that, (
      ()      => that,
      bool2   => (long1.serialize[Json] :: bool2.serialize[Json]   :: Nil).serialize[Json],
      long2   => (long1.serialize[Json] :: long2.serialize[Json]   :: Nil).serialize[Json],
      double2 => (long1.serialize[Json] :: double2.serialize[Json] :: Nil).serialize[Json],
      string2 => (long1.serialize[Json] :: string2.serialize[Json] :: Nil).serialize[Json],
      array2  => (long1.serialize[Json] :: array2.toList).serialize[Json],
      object2 => (long1.serialize[Json] :: object2.serialize[Json] :: Nil).serialize[Json]
    )),
    double1 => foldJson[Json](that, (
      ()      => that,
      bool2   => (double1.serialize[Json] :: bool2.serialize[Json]   :: Nil).serialize[Json],
      long2   => (double1.serialize[Json] :: long2.serialize[Json]   :: Nil).serialize[Json],
      double2 => (double1.serialize[Json] :: double2.serialize[Json] :: Nil).serialize[Json],
      string2 => (double1.serialize[Json] :: string2.serialize[Json] :: Nil).serialize[Json],
      array2  => (double1.serialize[Json] :: array2.toList).serialize[Json],
      object2 => (double1.serialize[Json] :: object2.serialize[Json] :: Nil).serialize[Json]
    )),
    string1 => foldJson[Json](that, (
      ()      => that,
      bool2   => (string1.serialize[Json] :: bool2.serialize[Json]   :: Nil).serialize[Json],
      long2   => (string1.serialize[Json] :: long2.serialize[Json]   :: Nil).serialize[Json],
      double2 => (string1.serialize[Json] :: double2.serialize[Json] :: Nil).serialize[Json],
      string2 => (string1.serialize[Json] :: string2.serialize[Json] :: Nil).serialize[Json],
      array2  => (string1.serialize[Json] :: array2.toList).serialize[Json],
      object2 => (string1.serialize[Json] :: object2.serialize[Json] :: Nil).serialize[Json]
    )),
    array1  => foldJson[Json](that, (
      ()      => that,
      bool2   => (array1.toList :+ bool2.serialize[Json]).serialize[Json],
      long2   => (array1.toList :+ long2.serialize[Json]).serialize[Json],
      double2 => (array1.toList :+ double2.serialize[Json]).serialize[Json],
      string2 => (array1.toList :+ string2.serialize[Json]).serialize[Json],
      array2  => (array1 ++ array2).serialize[Json],
      object2 => (array1.toList :+ object2.serialize[Json]).serialize[Json]
    )),
    object1 => foldJson[Json](that, (
      ()      => that,
      bool2   => (object1.serialize[Json] :: bool2.serialize[Json]   :: Nil).serialize[Json],
      long2   => (object1.serialize[Json] :: long2.serialize[Json]   :: Nil).serialize[Json],
      double2 => (object1.serialize[Json] :: double2.serialize[Json] :: Nil).serialize[Json],
      string2 => (object1.serialize[Json] :: string2.serialize[Json] :: Nil).serialize[Json],
      array2  => (object1.serialize[Json] :: array2.toList).serialize[Json],
      object2 => (object1.serialize[Json] :: object2.serialize[Json] :: Nil).serialize[Json]
    ))
  )

  private def FoldMerge(that: Json): JsonMatcher[Json] = (
    ()    => that,
    bool1 => foldJson[Json](that, (
      ()      => that,
      bool2   => bool2.serialize[Json],
      long2   => long2.serialize[Json],
      double2 => double2.serialize[Json],
      string2 => string2.serialize[Json],
      array2  => (bool1.serialize :: array2.toList).serialize[Json],
      object2 => object2.serialize[Json]
    )),
    long1   => foldJson[Json](that, (
      ()      => that,
      bool2   => bool2.serialize[Json],
      long2   => long2.serialize[Json],
      double2 => double2.serialize[Json],
      string2 => string2.serialize[Json],
      array2  => (long1.serialize :: array2.toList).serialize[Json],
      object2 => object2.serialize[Json]
    )),
    double1 => foldJson[Json](that, (
      ()      => that,
      bool2   => bool2.serialize[Json],
      long2   => long2.serialize[Json],
      double2 => double2.serialize[Json],
      string2 => string2.serialize[Json],
      array2  => (double1.serialize :: array2.toList).serialize[Json],
      object2 => object2.serialize[Json]
    )),
    string1 => foldJson[Json](that, (
      ()      => that,
      bool2   => bool2.serialize[Json],
      long2   => long2.serialize[Json],
      double2 => double2.serialize[Json],
      string2 => string2.serialize[Json],
      array2  => (string1.serialize :: array2.toList).serialize[Json],
      object2 => object2.serialize[Json]
    )),
    array1  => foldJson[Json](that, (
      ()      => that,
      bool2   => bool2.serialize[Json],
      long2   => long2.serialize[Json],
      double2 => double2.serialize[Json],
      string2 => string2.serialize[Json],
      array2  => (array1 ++ array2).serialize[Json],
      object2 => object2.serialize[Json]
    )),
    object1 => foldJson[Json](that, (
      ()      => that,
      bool2   => bool2.serialize[Json],
      long2   => long2.serialize[Json],
      double2 => double2.serialize[Json],
      string2 => string2.serialize[Json],
      array2  => (object1.serialize :: array2.toList).serialize[Json],
      object2 => {
        val m1 = object1.toMap
        val m2 = object2.toMap

        (m2.foldLeft(m1) { (all, tuple) =>
          val (name, value2) = tuple

          val merged = all.get(name).map { value1 =>
            foldJson(value1, FoldMerge(value2))
          }.getOrElse(value2)

          all + (name -> merged)
        }).serialize
      }
    ))
  )
}