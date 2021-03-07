(ns fae.world
  (:require
   [fae.engine :as engine]
   [fae.state :as state]
   [fae.grid :as grid]))


(defn instance []
  (let [dims [22 16]
        [w h] dims
        rot-wall-map (js/ROT.Map.Cellular. w h)
        rot-water-map (js/ROT.Map.Cellular. w h)]
    {:id     :world
     :dimensions dims
     :contents (do
                 (. rot-wall-map randomize 0.25)
                 (. rot-water-map randomize 0.4)
                 (. rot-water-map create nil)
                 (. rot-water-map create nil)
                 (let [wall-map (js->clj (.-_map rot-wall-map))
                       water-map (js->clj (.-_map rot-water-map))]


                   (vec (for [x (range 0 w)]
                          (vec (for [y (range 0 h)]
                                 (let [wall (get-in wall-map [x y])
                                       water (get-in water-map [x y])]
                                   (if (or (= x 0)
                                           (= y 0)
                                           (= x (dec w))
                                           (= y (dec h)))
                                     1
                                     (cond
                                       (= water 1) 2
                                       (= wall 1) 1
                                       :else 0)))))))))
     :graphics nil
     :init   (fn [world state]
               (let [{:keys [container] :as world} (assoc world :container (new js/PIXI.Container))
                     [w h] (:dimensions world)
                     contents (:contents world)
                     map (js/ROT.Map.Cellular. w h)]


                 (doseq [x (range 0 w)]
                   (doseq [y (range 0 h)]
                     (let [s (case (get-in contents [x y])
                               0 (engine/sprite "dot.png" [0 0])
                               1 (engine/sprite "wall.png" [0 0])
                               2 (engine/sprite "water.png" [0 0]))]
                       (.addChild container s)
                       (set! (.-x s) (* x grid/size))
                       (set! (.-y s) (* y grid/size)))))

                 (.addChild (:stage state) container)
                 (assoc world :graphics container)))
     :update (fn [world state])}))

(defn get-tile [state x y]
  (let [bgs (:background state)
        world (first bgs)
        contents (:contents world)]
    (get-in contents [x y])))
