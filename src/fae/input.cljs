(ns fae.input
  (:require
   [fae.events :as e]
   [clojure.string :as string]
   [fae.print :as print]
   [fae.sound :as sound]
   [fae.game :as game]
   [fae.state :as s]))

(string/lower-case "AAA")


;; determine which key pressed, what event it triggers and call actor handlers
(defn on-key-up! [_ev]
  ;; (js/console.log "keyup" ev))
  )

(defn on-key-down! [ev]
  (let [key (.-key ev)
        dispatch (fn [event-name]
                   (.preventDefault ev)
                   (e/trigger-event! event-name))]
    (case (string/lower-case key)
      "arrowup" (dispatch :move-up-pressed)
      "arrowdown" (dispatch :move-down-pressed)
      "arrowright" (dispatch :move-right-pressed)
      "arrowleft" (dispatch :move-left-pressed)
      "m" (sound/toggle-mute!)
      "w" (dispatch :tongue-up-pressed)
      "a" (dispatch :tongue-left-pressed)
      "s" (dispatch :tongue-down-pressed)
      "d" (dispatch :tongue-right-pressed)
      "j" (dispatch :jump-pressed)
      "r" (dispatch :restart-pressed)
      ;; "1" (println (map (fn [{:keys [id type]}] [id type]) (:actors @s/db)))
      ;; "2" (println (:progress @s/db))
      ;; "3" (println @e/inbox)
      ;; "4" (game/game-over!)
      ;; "5" (game/game-won!)
      nil)))

(defn attach-listeners []
  (print/events "> attached listeners")
  (.addEventListener js/window "keyup" on-key-up!)
  (.addEventListener js/window "keydown" on-key-down!))

(defn clear-listeners []
  (print/events "< cleared listeners")
  (.removeEventListener js/window "keyup" on-key-up!)
  (.removeEventListener js/window "keydown" on-key-down!))