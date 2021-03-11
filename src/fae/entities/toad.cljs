(ns fae.entities.toad
  (:require
   [fae.engine :as engine]
   [fae.behavior.id :as id]
   [fae.behavior.movement :as move]
   [fae.behavior.standard :as standard]
   [fae.grid :as grid]))

(defn init! [pos p _state] (move/set-initial-position p pos))

(defn update! [p _state] (move/smooth-move p))

(defn build-sprite []
  (let [spr (engine/sprite "toad.png" [0 0])]
    (set! (.-tint spr) 0xb1620e)
    spr))

(defn instance [_state [x y]]
  {:id       (id/generate!)
   :type     :toad

   :transform {:position {:x 0 :y 0}
               :rotation 0}

   :stats {:hp 5
           :speed 3
           :poisonous 2}
   :movement {:meter 0
              :move-fn move/handle-movement-random}

   :traits [:flying]
   :effects [:poison]
   :status []

   :grid {:x 0 :y 0}
   :graphics (build-sprite)
   :z-index  1

   :inbox []
   :events {:move-tick (fn [p state] (let [p (standard/handle-move-tick p state)]
                                       (if (< (rand-int 10) 2)
                                         (update p :status (fn [s] (conj s [:tired 3])))
                                         p)))
            :gained-poison standard/handle-gained-poison
            :licked-target standard/handle-licked-target
            :damaged standard/handle-damaged
            :bump standard/handle-bumped}
   :init   (partial init! [x y])
   :update update!})