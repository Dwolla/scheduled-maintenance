package com.dwolla.scheduledmaintenance

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("cross-fetch/polyfill", JSImport.Default)
object FetchPolyfill extends js.Any

/**
 * The reference to `FetchPolyfill` is a no-op here, but it forces the ScalaJS optimizer to include
 * the `FetchPolyfill` object (and therefore the import of `cross-fetch/polyfill`) in the generated
 * JS, instead of pruning what it thinks is an unused object.
 */
trait FetchPolyfill {
  FetchPolyfill
}
