/* Behaviour for the bundled offline fallback page.
 * Wired here instead of inline so the page needs no inline script execution. */
(function () {
    "use strict";

    function retry() {
        window.location.reload();
    }

    var button = document.getElementById("retry");
    if (button) {
        button.addEventListener("click", retry);
    }
})();