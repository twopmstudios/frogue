(ns fae.behavior.movement
  (:require [fae.print :as print]
            [fae.util :as util]
            [fae.grid :as grid]
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

(defn dir->vec [dir magnitude]
  (case dir
    :up [0 (- magnitude)]
    :down [0 magnitude]
    :left [(- magnitude) 0]
    :right [magnitude 0]))

(defn set-initial-position [actor [x y]]
  (-> actor
      (assoc-in [:grid :x] x)
      (assoc-in [:grid :y] y)
      (assoc-in [:transform :position :x] (* x grid/size))
      (assoc-in [:transform :position :y] (* y grid/size))))

(defn smooth-move [actor]
  (let [target-x (* grid/size (get-in actor [:grid :x]))
        target-y (* grid/size (get-in actor [:grid :y]))
        curr-x (get-in actor [:transform :position :x])
        curr-y (get-in actor [:transform :position :y])
        x (+ curr-x (/ (- target-x curr-x) 5))
        y (+ curr-y (/ (- target-y curr-y) 5))]
    (-> actor
        (assoc-in [:transform :position :x] x)
        (assoc-in [:transform :position :y] y))))

(defn get-actor-at [state x y]
  (let [actors (:actors state)
        at-pos (filter (fn [a] (and (= (get-in a [:grid :x]) x)
                                    (= (get-in a [:grid :y]) y)))
                       actors)]
    (first at-pos)))

(defn has-status? [actor status]
  (boolean (some (fn [[s _duration]] (= s status)) (:status actor))))

(defn move-grid [{:keys [grid id] :as actor} state x y]
  (if (has-status? actor :tired)
    actor
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
            (assoc-in [:grid :y] (+ (:y grid) y)))))))

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
              :damage (do (events/trigger-event! :damaged {:id (:id actor)
                                                           :amount 1})
                          act)
              :tire (update act :status (fn [s] (conj s [:tired 3])))
              act))
          actor effects))