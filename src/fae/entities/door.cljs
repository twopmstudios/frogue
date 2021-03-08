(ns fae.entities.door
  (:require
   [fae.engine :as engine]
   [fae.events :as events]
   [fae.behavior.id :as id]
   [fae.behavior.movement :as move]
   [fae.behavior.standard :as standard]
   [fae.grid :as grid]))

(defn init! [pos p _state] (move/set-initial-position p pos))

(defn update! [p _state] (move/smooth-move p))

(defn build-sprite []
  (let [spr (engine/sprite "door.png" [0 0])]
    spr))

(defn handle-bumped [g _state {bumpee :bumpee}]
  (if (= bumpee (:id g))
    (do (events/trigger-event! :door-entered)
        g)
    g))


(defn instance [_state [x y]]
  {:id       (id/generate!)
   :type     :door

   :transform {:position {:x 0 :y 0}
               :rotation 0}

   :stats {}

   :traits [:flying]
   :effects [:damage
             :tire]
   :status []

   :grid {:x 0 :y 0}
   :graphics (build-sprite)
   :z-index  1

   :inbox []
   :events {:bump handle-bumped}
   :init   (partial init! [x y])
   :update update!})