package rosetta.json

package object blueeyes {
  implicit val JsonImplementation: JsonImplementation[_root_.blueeyes.json.JsonAST.JValue] = JsonBlueEyes
}

package object dispatch {
  implicit val JsonImplementation: JsonImplementation[_root_.dispatch.json.JsValue] = JsonDispatch
}

package object lift {
  implicit val JsonImplementation: JsonImplementation[net.liftweb.json.JsonAST.JValue] = JsonLift
}


// vim: set ts=4 sw=4 et:
