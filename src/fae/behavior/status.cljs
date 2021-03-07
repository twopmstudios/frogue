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