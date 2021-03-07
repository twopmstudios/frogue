(ns fae.world
  (:require
   [fae.engine :as engine]
   [fae.state :as state]
   [fae.grid :as grid]))

(defn instance []
  (let [dims [22 16]
        [w h] dims]
    {:id     :world
     :dimensions dims
     :contents (vec (for [x (range 0 w)]
                      (vec (for [y (range 0 h)]
                             (if (or (= x 0)
                                     (= y 0)
                                     (= x (dec w))
                                     (= y (dec h))
                                     (and (= x 16) (= y 5))
                                     (and (= x 5) (= y 5)))
                               1
                               0)))))
     :init   (fn [world state]
               (let [{:keys [container] :as world} (assoc world :container (new js/PIXI.Container))
                     [w h] (:dimensions world)
                     contents (:contents world)]
                 (doseq [x (range 0 w)]
                   (doseq [y (range 0 h)]
                     (let [s (case (get-in contents [x y])
                               0 (engine/sprite "dot.png" [0 0])
                               1 (engine/sprite "wall.png" [0 0]))]
                       (.addChild container s)
                       (set! (.-x s) (* x grid/size))
                       (set! (.-y s) (* y grid/size)))))

                 (.addChild (:stage state) container)
                 world))
     :update (fn [world state])}))

(defn get-tile [state x y]
  (let [bgs (:background state)
        world (first bgs)
        contents (:contents world)]
    (get-in contents [x y])))
