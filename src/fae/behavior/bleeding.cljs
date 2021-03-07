(ns fae.behavior.bleeding
  (:require [fae.entities :as entities]
            [fae.events :as events]))

(defn handle [g]
  (let [status (:status g)
        bleeds (filter (fn [[effect _]] (= effect :bleeding)) status)
        total-dmg (reduce (fn [acc [_ dmg]] (+ acc dmg)) 0 bleeds)]
    (when (> total-dmg 0)
      (events/trigger-event! :damaged {:id (:id g) :amount total-dmg :source "bleed"})))
  g)