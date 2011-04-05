## Introduction

Rosetta Json is designed to allow *library authors* to create libraries that use the native types of any Json library. So instead of having to choose one of the many available Json libraries, library authors can instead write their code in a very portable way which works transparently across all Json libraries.

## Supported Json Libraries

Rosetta Json unifies across the following Json libraries:

 * [BlueEyes Json](github.com/jdegoes/blueeyes)
 * Lift Json
 * Dispatch Json

Rosetta does not wrap these types, which is an important distinction. Rather, libraries which use Rosetta Json will *natively* use any of the above Json types, without any changes to source code. This means users of your Rosetta-based libraries don't need to perform type conversions, because your library will automatically accept and return the same types used by their Json library of choice.

Rosetta makes it very easy to support new Json libraries - only about 10 values / methods need to be defined to support a new implementation.

Note: Rosetta is currently optimized for ease of supporting many Json libraries, not speed. In particular, it makes almost no assumptions about the underlying Json implementation. As a result, it's possible to create a Json implementation which stores Json values in String fragments. With this generality and ease of implementation comes a slight performance penalty, since Rosetta does not distinguish at the type level between different Json values, but instead relies on folds to extract "type" information.

## Usage

The core module is JsonImplementation, defined in package rosetta.json.

    import rosetta.json.JsonImplementation

This trait is parameterized by `Json`, which represents the supertype of all possible Json values.

You use this abstract type in your code, whether classes or traits:

    class MyJsonClass[Json: JsonImplementation] {
      import implicitly[JsonImplementation[Json]]._

      val json: Json = JsonNull
    }

    trait MyJsonTrait[Json] {
      def jsonImplementation: JsonImplementation[Json]

      import jsonImplementation._

      val json: Json = JsonNull
    }

## Serialization

Rosetta Json implementations support serialization of basic Scala data structures into the Json library types. Using the typeclass pattern, you can easily convert either from Scala types to Json, or from Json to Scala types:

    val json = Map("foo" -> 123L).serialize[Json]

    val object = json.deserialize[Iterable[(String, Long)]]

This allows you to create Json in an abstract, type-independent, and type-safe way.

The supported Scala types are listed below:

  * Boolean
  * String
  * Long
  * Double
  * Option of any supported type
  * Iterable of any supported type
  * Iterable of [String, any supported type]

In addition, Rosetta Json allows you to convert between Json and String:

    val string = JsonNull.serialize[String]

    val json = string.deserialize[Json]

With a little help from rosetta.io.Serializers, you can go all the way to bytes:

    import rosetta.io.Serializers.StringToArrayByte

    implicit val JsonToByteArray = JsonToString >>> StringToArrayByte("UTF-8")

    val bytes = JsonNull.serialize[Array[Byte]]

## Manipulation

You can also create and pattern match against the following pseudo-types:

  * JsonNull
  * JsonLong
  * JsonDouble
  * JsonString
  * JsonArray
  * JsonObject

<pre>
json match {
  case JsonObject(fields) => JsonObject(fields.map(_._1 != "forbidden"))
}
</pre>

Thanks to a pimp, you may also invoke a variety of methods directly on Json values:

  * foldUp
  * foldDown
  * mapUp
  * mapDown
  * get

These methods are sufficient for the majority of library authors. If you need additional methods, you can usually define them in terms of one of the folds, and if you find the same pattern frequently, please contact me and I'll get back to you.

## Team

Rosetta Json is currently maintained by me (John A. De Goes). If you are interested in adding support for new Json libraries, or other features, feel free to contact me.

<table>
  <thead>
    <tr>
      <td>Name</td>               <td>Role</td>       <td>Twitter</td>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>John A. De Goes</td>    <td>Author</td>     <td><a href="http://twitter.com/jdegoes">@jdegoes</a></td>
    </tr>
  </tbody>
</table>