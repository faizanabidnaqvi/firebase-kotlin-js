@file:JsModule("firebase/app")
@file:JsNonModule
package firebase

import firebase.app.App
import firebase.firestore.Firestore

external fun app(name: String? = definedExternally): App
external fun initializeApp(options: Any, name: String? = definedExternally): App
external fun firestore(app: App? = definedExternally): Firestore