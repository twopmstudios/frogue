(ns fae.behavior.status
  (:require [fae.print :as print]
            [fae.util :as util]
            [fae.entities :as entities]
            [fae.grid :as grid]
            [fae.events :as events]))

(defn tickdown [actor]
  (let [status (:status actor)
        ;; iterate over list decrementing duration timers
        status' (map (fn [[effect duration]] [effect (dec duration)]) status)
        ;; remove all values from list that are <= 0
        status'' (filter (fn [[_ duration]] (> duration 0)) status')]
    (assoc actor :status status'')))


;; 1. entites have a poisonous stat
;; 2. attacking an entity with poisonous > 0 -> taking large dmg & gaining +1 to poisonous stat
;; 3. being attacked when poisonous -> tick down poisonous stat by 1