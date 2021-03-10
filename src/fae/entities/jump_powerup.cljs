(ns fae.entities.jump-powerup
  (:require
   [fae.engine :as engine]
   [fae.events :as events]
   [fae.entities :as entities]
   [fae.world :as world]
   [fae.behavior.id :as id]
   [fae.behavior.movement :as move]
   [fae.behavior.standard :as standard]
   [fae.grid :as grid]))

(defn init! [pos p _state] (move/set-initial-position p pos))

(defn update! [p _state] (move/smooth-move p))

(defn build-sprite []
  (let [spr (engine/sprite "powerup.png" [0 0])]
    spr))

(defn handle-bumped [powerup state {bumpee :bumpee
                                    bumper-id :bumper}]
  (let [bumper (entities/get-by-id bumper-id state)]
    (if (and (= :player (:type bumper))
             (= bumpee (:id powerup)))
      (do (events/trigger-event! :powerup-get {:kind :jump})
          (events/trigger-event! :log-entry-posted {:msg "You feel a spring in your step."})
          (events/trigger-event! :log-entry-posted {:msg "You can now jump using J + Arrows."})
          (entities/remove-entity! (:id powerup))
          powerup)
      powerup)))

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