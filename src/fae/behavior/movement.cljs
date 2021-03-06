(ns fae.behavior.movement
  (:require [fae.print :as print]
            [fae.events :as events]))

(defn perform [g state move-fn]
  ;; add speed to movement meter
  ;; use integer meter value to move
  ;; carry over floating part
  (let [g' (update-in g [:movement :meter]
                      (fn [m] (+ m (get-in g [:stats :speed]))))
        meter' (get-in g' [:movement :meter])
        movement (js/Math.floor meter')
        meter'' (- meter' movement)
        g'' (assoc-in g' [:movement :meter] meter'')]

    ;; (print/debug (str meter'' "," movement))

    (move-fn g'' state movement)))

(defn get-actor-at [state x y]
  (let [actors (:actors state)
        at-pos (filter (fn [a] (and (= (get-in a [:grid :x]) x)
                                    (= (get-in a [:grid :y]) y)))
                       actors)]
    (first at-pos)))

(defn move-grid [{:keys [grid id] :as actor} state x y]
  (let [[nx ny] [(+ (:x grid) x) (+ (:y grid) y)]
        occupant (get-actor-at state nx ny)]

    (if occupant
      (do
        (events/trigger-event! :bump {:position {:x nx :y ny}
                                      :bumper id
                                      :bumpee (:id occupant)
                                      :effects (:effects actor)})
        actor)
      (-> actor
          (assoc-in [:grid :x] (+ (:x grid) x))
          (assoc-in [:grid :y] (+ (:y grid) y))))))

(defn raycast [[ox oy] state dir length]
  (let [first-hit (fn [f range] (first (filter some? (map f range))))
        r (case dir
            :up (reverse (range (- oy length) oy))
            :down (range (inc oy) (+ (inc oy) length))
            :left (reverse (range (- ox length) ox))
            :right (range (inc ox) (+ (inc ox) length)))]

    (println "raycast" [ox oy] dir r)

    (case dir
      :up  (first-hit (fn [i] (get-actor-at state ox i)) r)
      :down (first-hit (fn [i] (get-actor-at state ox i)) r)
      :left (first-hit (fn [i] (get-actor-at state i oy)) r)
      :right (first-hit (fn [i] (get-actor-at state i oy)) r))))

(defn bumped [actor effects]
  (println "bumped" (:id actor) effects)
  (reduce (fn [act e]
            (println "bump effect" e)
            (case e
              :damage act
              :tire (update act :status (fn [s] (conj s [:tired 3])))
              act))
          actor effects))