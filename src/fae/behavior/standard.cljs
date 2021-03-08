(ns fae.behavior.standard
  (:require [fae.entities :as entities]
            [fae.behavior.movement :as move]
            [fae.behavior.licked :as lick]
            [fae.behavior.damaged :as damage]
            [fae.behavior.bleeding :as bleed]
            [fae.behavior.status :as status]
            [fae.events :as events]))

(defn handle-move-tick
  [g state]
  (if (= :player (:type g))
    (-> g
        (bleed/handle)
        (status/tickdown))
    (-> g
        (move/perform state (get-in g [:movement :move-fn]))
        (bleed/handle)
        (status/tickdown))))

(defn handle-licked-target
  [g state {target-id :id
            dmg :dmg}]
  (if (= (:id g) target-id)
    (lick/handle g dmg)
    g))

(defn handle-damaged
  [g state {id :id
            amount :amount
            source :source}]
  (if (= id (:id g)) (damage/handle g amount source) g))

(defn handle-bumped [g state {bumpee :bumpee
                              bumper :bumper
                              effects :effects}]
  (if (= bumpee (:id g))
    (move/bumped g state effects bumper)
    g))