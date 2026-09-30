Ajax
====
Ajax components for WebObjects, built on the bundled Prototype and
script.aculo.us JavaScript libraries.

Local changes to bundled JavaScript
-----------------------------------
Review these before upgrading `prototype.js` or the other bundled
libraries.

* `wonder_inplace.js` overrides features of `Ajax.InPlaceEditor`.
* `wonder.js`:
  * `Ajax.StoppedPeriodicalUpdater` extends `Ajax.Base`.
  * `Ajax.ActivePeriodicalUpdater` extends `Ajax.Base`.
  * `Form.Element.RadioButtonObserver` extends
    `Form.Element.EventObserver`.
  * `AjaxSubmitButton.observeField` works around a Prototype bug with
    observing radio buttons.
  * `Prototype.exec` is brought forward.
* `prototype.js`: in `Form.Element.Methods.serialize`,
  `element.getValue()` failed in an unusual case, so it calls
  `Form.Element.Methods.getValue(element)` instead.
* `ibox.js`:
  * Adds a `_pub.init` function so iBox can be reinitialised.
  * Adds the `X-Requested-With` header so iBox requests are recognised
    as Ajax requests.
* `controls.js` replaces `Autocompleter.markPrevious` and `markNext`,
  and adds a line to `getEntry` (Wonder-406).
