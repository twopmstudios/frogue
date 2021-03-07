(ns fae.entities.mosquito
  (:require
   [fae.engine :as engine]
   [fae.print :as print]
   [fae.events :as e]
   [fae.entities :as entities]
   [fae.behavior.standard :as standard]
   [fae.behavior.id :as id]
   [fae.behavior.movement :as move]
   [fae.grid :as grid]))

(defn init! [pos p _state] (move/set-initial-position p pos))

(defn update! [p _state] (move/smooth-move p))

(defn build-sprite []
  (engine/sprite "mosquito.png" [0 0]))

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

(defn handle-movement [g state movement]
  (let [player-pos (:grid (entities/get-by-type :player))
        next-to-player (adjacent? player-pos (:grid g))
        d (diff player-pos (:grid g))]

    (println "mosquito" next-to-player d)
    (if next-to-player
      (move/move-grid g state (d :x) (d :y))
      (let [dir (rand-nth [:up :down :left :right])
            [x y] (move/dir->vec dir movement)]
        (if (> movement 0)
          (move/move-grid g state x y)
          g)))))


(defn instance [_state [x y]]
  {:id       (id/generate!)
   :type     :mosquito

   :transform {:position {:x 0 :y 0}
               :rotation 0}

   :stats {:hp 5
           :speed 0.5}
   :movement {:meter 0
              :move-fn handle-movement}

   :effects [:damage]
   :status []

   :grid {:x 0 :y 0}
   :graphics (build-sprite)
   :z-index  1

   :inbox []
   :events {:move-tick standard/handle-move-tick
            :licked-target standard/handle-licked-target
            :damaged standard/handle-damaged
            :bump standard/handle-bumped}
   :init   (partial init! [x y])
   :update update!})

