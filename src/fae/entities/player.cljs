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

(defn move [{{:keys [position]} :transform :as player} x y]
  (-> player
      (assoc-in [:transform :position :x] (+ (:x position) x))
      (assoc-in [:transform :position :y] (+ (:y position) y))))

(defn move-grid [{:keys [grid] :as player} x y]
  (e/trigger-event! :move-tick)

  (-> player
      (assoc-in [:grid :x] (+ (:x grid) x))
      (assoc-in [:grid :y] (+ (:y grid) y))))

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

    (println "tongue" new-pos)
    (when-let [target (raycast [(:x curr-pos) (:y curr-pos)] state dir tongue)]
      (println "licked" (:id target) target)
      (e/trigger-event! :licked-target {:id (:id target)
                                        :dmg (get-in p [:stats :lick])}))

    (-> p
        (assoc-in [:tongue :active] true)
        (assoc-in [:tongue :target] new-pos))))

(defn init! [[x y] p _state]
  (-> p
      (assoc-in [:grid :x] x)
      (assoc-in [:grid :y] y)
      (assoc-in [:transform :position :x] (* x grid/size))
      (assoc-in [:transform :position :y] (* y grid/size))))

(defn update! [p _state]
  (let [target-x (* grid/size (get-in p [:grid :x]))
        target-y (* grid/size (get-in p [:grid :y]))
        curr-x (get-in p [:transform :position :x])
        curr-y (get-in p [:transform :position :y])
        x (+ curr-x (/ (- target-x curr-x) 5))
        y (+ curr-y (/ (- target-y curr-y) 5))]
    (-> p
        (assoc-in [:transform :position :x] x)
        (assoc-in [:transform :position :y] y))))

(defn build-sprite []
  (let [spr (engine/sprite "at.png" [0 0])]
    ;; (set! (.-tint spr) (rand-int 16rFFFFFF))
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

   :stats {:egg 200
           :lick 2
           :tongue 2}

   :inbox []
   :events {:move-up-pressed (fn [p _state] (move-grid p 0 -1))
            :move-down-pressed (fn [p _state] (move-grid p 0 1))
            :move-right-pressed (fn [p _state] (move-grid p 1 0))
            :move-left-pressed (fn [p _state] (move-grid p -1 0))

            :tongue-up-pressed (fn [p state] (shoot-tongue p state :up))
            :tongue-down-pressed (fn [p state] (shoot-tongue p state :down))
            :tongue-left-pressed (fn [p state] (shoot-tongue p state :left))
            :tongue-right-pressed (fn [p state] (shoot-tongue p state :right))}
   :init   (partial init! [x y])
   :update update!})
