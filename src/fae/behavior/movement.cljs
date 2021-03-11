(ns fae.behavior.movement
  (:require [fae.print :as print]
            [fae.util :as util]
            [fae.entities :as entities]
            [fae.grid :as grid]
            [fae.world :as world]
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
  (let [[nx ny] [(+ (:x grid) x) (+ (:y grid) y)]]
    (if (or (has-status? actor :tired) ;; tired -> can't move
            (= 1 (world/get-tile state nx ny)) ;; walls -> can't move
            (= nil (world/get-tile state nx ny)) ;; void -> can't move
            (and (= 2 (world/get-tile state nx ny)) ;; water & non-flying -> can't move
                 (not (util/in? (:traits actor) :flying))))
      actor
      (let [occupant (get-actor-at state nx ny)]

        (if occupant
          (do
            (events/trigger-event! :bump {:position {:x nx :y ny}
                                          :bumper id
                                          :bumpee (:id occupant)
                                          :effects (:effects actor)})
            actor)
          (-> actor
              (assoc-in [:grid :x] (+ (:x grid) x))
              (assoc-in [:grid :y] (+ (:y grid) y))))))))

(defn raycast [[ox oy] state dir length]
  (let [first-hit (fn [f range] (first (filter some? (map f range))))
        r (case dir
            :up (reverse (range (- oy length) oy))
            :down (range (inc oy) (+ (inc oy) length))
            :left (reverse (range (- ox length) ox))
            :right (range (inc ox) (+ (inc ox) length)))]

    (println "raycast" [ox oy] dir r)

    (case dir
      :up  (first-hit (fn [i]
                        (or (get-actor-at state ox i)
                            (when (= 1 (world/get-tile state ox i))
                              :wall))) r)
      :down (first-hit (fn [i]
                         (or (get-actor-at state ox i)
                             (when (= 1 (world/get-tile state ox i))
                               :wall))) r)
      :left (first-hit (fn [i]
                         (or (get-actor-at state i oy)
                             (when (= 1 (world/get-tile state i oy))
                               :wall))) r)
      :right (first-hit (fn [i]
                          (or (get-actor-at state i oy)
                              (when (= 1 (world/get-tile state i oy))
                                :wall))) r))))

;; (:type (entities/get-by-id 10))

(defn bumped [actor state effects other-id]
  (let [bumper (entities/get-by-id other-id state)]
    (println "bumped" (:id actor) effects other-id)

  ;; if actor has poisonous stat -> deal damage to other-id
    (when-let [dmg (get-in actor [:stats :poisonous])]
      (when (> dmg 0)
        (if (= (:type bumper) :player)
          (events/trigger-event! :log-entry-posted {:msg (util/format "You touched poison!")})
          (events/trigger-event! :log-entry-posted {:msg (util/format "%s touched poison!" (:type bumper))}))

        (events/trigger-event! :damaged {:id (:id bumper)
                                         :amount (get-in actor [:stats :poisonous])
                                         :source "poison"})

        (events/trigger-event! :gained-poison {:id (:id bumper)})))

    (reduce (fn [act e]
              (case e
                :damage (do (events/trigger-event! :damaged {:id (:id actor)
                                                             :amount 1
                                                             :source (:type bumper)})
                            act)
                :tire (do (if (= (:type actor) :player)
                            (events/trigger-event! :log-entry-posted {:msg (util/format "You feel tired (3 turns)")})
                            (events/trigger-event! :log-entry-posted {:msg (util/format "%s feels tired (3 turns)" (:type actor))}))
                          (update act :status (fn [s] (conj s [:tired 3]))))
                :bleed (do (if (= (:type actor) :player)
                             (events/trigger-event! :log-entry-posted {:msg (util/format "You are bleeding (3 turns)")})
                             (events/trigger-event! :log-entry-posted {:msg (util/format "%s is bleeding (3 turns)" (:type actor))}))
                           (update act :status (fn [s] (conj s [:bleeding 3]))))

                :venom (do
                         (if (= (:type actor) :player)
                           (events/trigger-event! :log-entry-posted {:msg (util/format "You gain +1 poison!")})
                           (events/trigger-event! :log-entry-posted {:msg (util/format "%s gains +1 poison!" (:type actor))}))

                         (events/trigger-event! :damaged {:id (:id actor)
                                                          :amount 1
                                                          :source "venom"})

                         ;; TODO: remove the update-in and handle event in all actors
                         (events/trigger-event! :gained-poison {:id (:id actor)})
                         act)
                act))
            actor effects)))

(defn abs [n] (max n (- n)))

(defn adjacent? [a b]
  (let [ax (:x a)
        ay (:y a)
        bx (:x b)
        by (:y b)]
    (or
     (and (= ax bx) (= 1 (abs (- ay by))))
     (and (= ay by) (= 1 (abs (- ax bx)))))))

(defn diff [a b]
  (let [ax (:x a)
        ay (:y a)
        bx (:x b)
        by (:y b)]
    {:x (- ax bx) :y (- ay by)}))

(defn handle-movement-aggressive [g state movement]
  (let [player-pos (:grid (entities/get-by-type :player state))
        next-to-player (adjacent? player-pos (:grid g))
        d (diff player-pos (:grid g))]

    (if next-to-player
      (move-grid g state (d :x) (d :y))
      (let [dir (rand-nth [:up :down :left :right])
            [x y] (dir->vec dir movement)]
        (if (> movement 0)
          (move-grid g state x y)
          g)))))

(defn handle-movement-random [g state movement]
  (let [dir (rand-nth [:up :down :left :right])
        [x y] (dir->vec dir movement)]
    (if (> movement 0)
      (move-grid g state x y)
      g)))