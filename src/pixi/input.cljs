(ns pixi.input
  (:require
   [pixi.events :as e]
   [pixi.state :as s]))

;; determine which key pressed, what event it triggers and call actor handlers
(defn on-key-up! [ev]
  ;; (js/console.log "keyup" ev)
  (let [key (.-key ev)]
    (case key
      "ArrowUp" (e/trigger-event! s/state :move-up-pressed)
      "ArrowDown" (e/trigger-event! s/state :move-down-pressed)
      "ArrowRight" (e/trigger-event! s/state :move-right-pressed)
      "ArrowLeft" (e/trigger-event! s/state :move-left-pressed)
      nil)))

(defn on-key-down! [_ev])

(defn attach-listeners []
  (js/console.info "> attached listeners")
  (.addEventListener js/window "keyup" on-key-up!))

(defn clear-listeners []
  (js/console.info "< cleared listeners")
  (.removeEventListener js/window "keyup" on-key-up!))