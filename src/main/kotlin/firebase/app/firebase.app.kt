@file:JsModule("firebase/app")
@file:JsNonModule
@file:JsQualifier("app")
package firebase.app

import firebase.firestore.Firestore

external interface App {
    var name: String
    var options: Any
    fun firestore(): Firestore
}
