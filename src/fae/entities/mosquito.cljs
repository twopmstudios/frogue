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
  (let [spr (engine/sprite "mosquito.png" [0 0])]
    (set! (.-tint spr) 0xaaaaaa)
    spr))

(defn instance [_state [x y]]
  {:id       (id/generate!)
   :type     :mosquito

   :transform {:position {:x 0 :y 0}
               :rotation 0}

   :stats {:hp 5
           :speed 0.5}
   :movement {:meter 0
              :move-fn move/handle-movement-aggressive}

   :traits [:flying]
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

