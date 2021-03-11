(ns fae.entities.other-frog
  (:require
   [fae.engine :as engine]
   [fae.events :as events]
   [fae.entities :as entities]
   [fae.world :as world]
   [fae.behavior.id :as id]
   [fae.behavior.movement :as move]
   [fae.behavior.standard :as standard]
   [fae.grid :as grid]))

(defn init! [pos p _state]
  (events/trigger-event! :doors-locked)
  (move/set-initial-position p pos))

(defn update! [p _state] (move/smooth-move p))

(defn build-sprite []
  (let [spr (engine/sprite "other-frog.png" [0 0])]
    spr))

(defn handle-bumped [frog state {bumpee :bumpee
                                 bumper-id :bumper}]
  (let [bumper (entities/get-by-id bumper-id state)]
    (if (and (= :player (:type bumper))
             (= bumpee (:id frog)))
      (do
        (events/trigger-event! :log-entry-posted {:msg "You bump & grind with the frog"})
        (events/trigger-event! :log-entry-posted {:msg "Your eggs feel... uh... fertile"})
        (events/trigger-event! :eggs-fertilized)
        (entities/remove-entity! (:id frog))
        frog)
      frog)))


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