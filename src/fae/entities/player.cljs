(ns fae.entities.player
  (:require
   [fae.engine :as engine]
   [fae.events :as e]
   [fae.state :as s]
   [fae.print :as print]
   [fae.behavior.movement :as move]
   [fae.behavior.id :as id]
   [fae.grid :as grid]))

(defn ship-icon []
  (doto (new js/PIXI.Graphics)
    (.beginFill 0x3355ff 0.5)
    (.lineStyle 3 0xFF5500)
    (.moveTo -12.5 -10)
    (.lineTo 12.5 0)
    (.lineTo -12.5 10)
    (.lineTo -12.5 -10)
    (.endFill)))



(defn move-grid [player state x y]
  (e/trigger-event! :move-tick)
  (move/move-grid player state x y))

(defn raycast [[ox oy] state dir length]
  (let [first-hit (fn [f range] (first (filter some? (map f range))))
        r (case dir
            :up (reverse (range (- oy length) oy))
            :down (range (inc oy) (+ (inc oy) length))
            :left (reverse (range (- ox length) ox))
            :right (range (inc ox) (+ (inc ox) length)))]

    (println "raycast" [ox oy] dir r)

    (case dir
      :up  (first-hit (fn [i] (move/get-actor-at state ox i)) r)
      :down (first-hit (fn [i] (move/get-actor-at state ox i)) r)
      :left (first-hit (fn [i] (move/get-actor-at state i oy)) r)
      :right (first-hit (fn [i] (move/get-actor-at state i oy)) r))))

(defn shoot-tongue [p state dir]
  (let [tongue (get-in p [:stats :tongue])
        [tx ty] (case dir
                  :up [0 (- tongue)]
                  :down [0 tongue]
                  :left [(- tongue) 0]
                  :right [tongue 0])

        curr-pos (get p :grid)
        new-pos {:x (+ (:x curr-pos) tx) :y (+ (:y curr-pos) ty)}]

    (e/trigger-event! :move-tick)
    (println "tongue" new-pos)
    (when-let [target (raycast [(:x curr-pos) (:y curr-pos)] state dir tongue)]
      (println "licked" (:id target) target)
      (e/trigger-event! :licked-target {:id (:id target)
                                        :dmg (get-in p [:stats :lick])}))

    (-> p
        (assoc-in [:tongue :active] true)
        (assoc-in [:tongue :target] new-pos))))

(defn init! [pos p _state] (move/set-initial-position p pos))

(defn update! [p _state] (move/smooth-move p))

(defn build-sprite []
  (let [spr (engine/sprite "at.png" [0 0])]
    (set! (.-tint spr) (rand-int 16rFFFFFF))
    spr))

(defn instance [_state [x y]]
  {:id       (id/generate!)
   :type     :player
   :transform {:position {:x 0 :y 0}
               :rotation 0}
   :grid {:x 0 :y 0}
   :graphics (build-sprite)
  ;;  :rotate-constantly (/ (+ x y) 2000.0)
   :z-index  1
   :tongue {:active false
            :target {:x 0 :y 0}}

   :stats {:eggs 200
           :size 10
           :lick 2
           :tongue 2}

   :effects [:damage]
   :status []

   :inbox []
   :events {:move-up-pressed (fn [p state] (move-grid p state 0 -1))
            :move-down-pressed (fn [p state] (move-grid p state 0 1))
            :move-right-pressed (fn [p state] (move-grid p state 1 0))
            :move-left-pressed (fn [p state] (move-grid p state -1 0))

            :tongue-up-pressed (fn [p state] (shoot-tongue p state :up))
            :tongue-down-pressed (fn [p state] (shoot-tongue p state :down))
            :tongue-left-pressed (fn [p state] (shoot-tongue p state :left))
            :tongue-right-pressed (fn [p state] (shoot-tongue p state :right))

            :damaged (fn [g state {id :id
                                   amount :amount}]
                       (if (= id (:id g))
                         (let [pierced (- (get-in g [:stats :size]) amount)]
                           (println "pierced" pierced)
                           (if (< pierced 0)
                             (-> g
                                 (assoc-in [:stats :size] 0)
                                 (update-in [:stats :eggs] (fn [e] (+ e pierced))))
                             (assoc-in g [:stats :size] pierced)))
                         g))

            :bump (fn [g state {bumpee :bumpee
                                effects :effects}]
                    (if (= bumpee (:id g))
                      (move/bumped g effects)
                      g))}
   :init   (partial init! [x y])
   :update update!})
