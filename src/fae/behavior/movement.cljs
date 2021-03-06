(ns fae.behavior.movement
  (:require [fae.print :as print]))

(defn perform [g state move-fn]
  (println "do move")
  ;; add speed to movement meter
  ;; use integer meter value to move
  ;; carry over floating part
  (let [g' (update-in g [:movement :meter]
                      (fn [m] (+ m (get-in g [:stats :speed]))))
        meter' (get-in g' [:movement :meter])
        movement (js/Math.floor meter')
        meter'' (- meter' movement)
        g'' (assoc-in g' [:movement :meter] meter'')]

    (print/debug (str meter'' "," movement))

    (move-fn g'' state movement)))

(defn get-actor-at [state x y]
  (let [actors (:actors state)
        at-pos (filter (fn [a] (and (= (get-in a [:grid :x]) x)
                                    (= (get-in a [:grid :y]) y)))
                       actors)]
    (first at-pos)))
