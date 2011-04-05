## Introduction

Rosetta Json is designed to allow *library authors* to create libraries that use the native types of any Json library. So instead of having to choose one of the many available Json libraries, library authors can instead write their code in a very portable way which works transparently across all Json libraries.

## Supported Json Libraries

Rosetta Json unifies across the following Json libraries:

 * BlueEyes Json
 * Lift Json
 * Dispatch Json

Rosetta does not wrap these types, which is an important distinction. Rather, libraries which use Rosetta Json will *natively* use any of the above Json types, without any changes to source code.

## Usage

The core module is JsonImplementation, defined in package rosetta.json.

    import rosetta.json.JsonImplementation

This trait is parameterized by <pre>Json</pre>, which represents the supertype of all possible Json values.

You use this abstract type in your code, whether classes or traits:

    class MyJsonClass[Json: JsonImplementation] {
      import implicitly[JsonImplementation[Json]]._

      val json: Json = JsonNull
    }

    trait MyJsonTrait[Json: JsonImplementation] {
      import implicitly[JsonImplementation[Json]]._

      val json: Json = JsonNull
    }

## Serialization

Rosetta Json implementations support serialization of basic Scala data structures into the Json library types. Using the typeclass pattern, you can easily convert either from Scala types to Json, or from Json to Scala types:

    import implicitly[JsonImplementation[Json]]._

    val json = Map("foo" -> 123L).serialize

    val object = json.deserialize[Iterable[(String, Long)]]

This allows you to create Json in an abstract, type-independent, and type-safe way.

## Manipulation

You can also create and pattern match against the following pseudo-types:

  * JsonNull
  * JsonLong
  * JsonDouble
  * JsonString
  * JsonArray
  * JsonObject

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
      <td>Name</td>               <td>Role</td>                                                                    <td>Twitter</td>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>John A. De Goes</td>    <td>Author</td>                                    <td><a href="http://twitter.com/jdegoes">@jdegoes</a></td>
    </tr>
  </tbody>
</table>