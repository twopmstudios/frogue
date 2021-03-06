(ns fae.entities.gnat
  (:require
   [fae.engine :as engine]
   [fae.print :as print]
   [fae.grid :as grid]))

(defn move [{{:keys [position]} :transform :as player} x y]
  (-> player
      (assoc-in [:transform :position :x] (+ (:x position) x))
      (assoc-in [:transform :position :y] (+ (:y position) y))))

(defn move-grid [{:keys [grid] :as player} x y]
  (-> player
      (assoc-in [:grid :x] (+ (:x grid) x))
      (assoc-in [:grid :y] (+ (:y grid) y))))

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
  (engine/sprite "gnat.png" [0 0]))

(defn instance [_state [x y]]
  {:id       :gnat
   :type     :gnat

   :transform {:position {:x 0 :y 0}
               :rotation 0}

   :stats {:speed 1.5}
   :movement {:meter 0}

   :grid {:x 0 :y 0}
   :graphics (build-sprite)
  ;;  :rotate-constantly (/ (+ x y) 2000.0)
   :z-index  1

   :inbox []
   :events {:move-tick (fn [g _state]
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

                           (if (> movement 0)
                             (update-in g'' [:grid :x] (fn [x] (+ x movement)))
                             g'')))}
   :init   (partial init! [x y])
   :update update!})
