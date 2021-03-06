(ns fae.input
  (:require
   [fae.events :as e]
   [fae.print :as print]
   [fae.entities :as entities]
   [fae.entities.gnat :as gnat]
   [fae.state :as s]))

;; determine which key pressed, what event it triggers and call actor handlers
(defn on-key-up! [_ev]
  ;; (js/console.log "keyup" ev))
  )

(defn on-key-down! [ev]
  (let [key (.-key ev)
        dispatch (fn [event-name]
                   (.preventDefault ev)
                   (e/trigger-event! event-name))]
    (case key
      "ArrowUp" (dispatch :move-up-pressed)
      "ArrowDown" (dispatch :move-down-pressed)
      "ArrowRight" (dispatch :move-right-pressed)
      "ArrowLeft" (dispatch :move-left-pressed)
      "w" (dispatch :tongue-up-pressed)
      "a" (dispatch :tongue-left-pressed)
      "s" (dispatch :tongue-down-pressed)
      "d" (dispatch :tongue-right-pressed)
      "1" (println (map (fn [{:keys [id type]}] [id type]) (:actors @s/db)))
      "2" (println (:actors @s/db))
      "3" (entities/add-entity! (gnat/instance @s/db [3 3]))
      nil)))

(defn attach-listeners []
  (print/events "> attached listeners")
  (.addEventListener js/window "keyup" on-key-up!)
  (.addEventListener js/window "keydown" on-key-down!))

(defn clear-listeners []
  (print/events "< cleared listeners")
  (.removeEventListener js/window "keyup" on-key-up!)
  (.removeEventListener js/window "keydown" on-key-down!))