(ns fae.player
  (:require
   [fae.engine :as engine]
   [fae.print :as print]
   [fae.grid :as grid]))

(defn ship-icon []
  (doto (js/PIXI.Graphics.)
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
  (-> player
      (assoc-in [:grid :x] (+ (:x grid) x))
      (assoc-in [:grid :y] (+ (:y grid) y))))

(defn update! [p state]
  (let [target-x (* grid/size (get-in p [:grid :x]))
        target-y (* grid/size (get-in p [:grid :y]))
        curr-x (get-in p [:transform :position :x])
        curr-y (get-in p [:transform :position :y])
        x (+ curr-x (/ (- target-x curr-x) 5))
        y (+ curr-y (/ (- target-y curr-y) 5))
        rotation (get-in p [:transform :rotation])
        rotation' (+ rotation 0.01)]
    (-> p
        (assoc-in [:transform :position :x] x)
        (assoc-in [:transform :position :y] y)
        (assoc-in [:transform :rotation] rotation'))))

(defn instance [_state [x y]]
  {:id       :player
   :type     :player
   :transform {:position {:x 0 :y 0}
               :rotation 15}
   :grid {:x 0 :y 0}
   :graphics (engine/sprite "at.png" [0 0])
   :velocity {:y 0 :x 0}
   :z-index  1
   :init     (fn [p _state]
               (-> p
                   (assoc-in [:grid :x] x)
                   (assoc-in [:grid :y] y)
                   (assoc-in [:transform :position :x] (* x grid/size))
                   (assoc-in [:transform :position :y] (* y grid/size))))
   :events {:move-up-pressed (fn [p _state] (move-grid p 0 -1))
            :move-down-pressed (fn [p _state] (move-grid p 0 1))
            :move-right-pressed (fn [p _state] (move-grid p 1 0))
            :move-left-pressed (fn [p _state] (move-grid p -1 0))}
   :update update!})
