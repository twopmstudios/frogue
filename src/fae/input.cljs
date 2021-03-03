(ns fae.input
  (:require
   [fae.events :as e]
   [fae.print :as print]
   [fae.state :as s]))

;; determine which key pressed, what event it triggers and call actor handlers
(defn on-key-up! [_ev]
  ;; (js/console.log "keyup" ev))
  )

(defn on-key-down! [ev]
  (let [key (.-key ev)]
    (case key
      "ArrowUp" (e/trigger-event! s/db :move-up-pressed)
      "ArrowDown" (e/trigger-event! s/db :move-down-pressed)
      "ArrowRight" (e/trigger-event! s/db :move-right-pressed)
      "ArrowLeft" (e/trigger-event! s/db :move-left-pressed)
      nil)))

(defn attach-listeners []
  (print/events "> attached listeners")
  (.addEventListener js/window "keyup" on-key-up!)
  (.addEventListener js/window "keydown" on-key-down!))

(defn clear-listeners []
  (print/events "< cleared listeners")
  (.removeEventListener js/window "keyup" on-key-up!)
  (.removeEventListener js/window "keydown" on-key-down!))