package rosetta.json

package object blueeyes {
  implicit val JsonBlueEyes: JsonImplementation[_root_.blueeyes.json.JsonAST.JValue] = blueeyes.JsonBlueEyes
}

package object dispatch {
  implicit val JsonDispatch: JsonImplementation[_root_.dispatch.json.JsValue] = dispatch.JsonDispatch
}

package object lift {
  implicit val JsonLift: JsonImplementation[net.liftweb.json.JsonAST.JValue] = lift.JsonLift
}


// vim: set ts=4 sw=4 et:
