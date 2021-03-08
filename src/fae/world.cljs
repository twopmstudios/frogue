(ns fae.world
  (:require
   [fae.engine :as engine]
   [fae.state :as state]
   [fae.grid :as grid]))

(def EMPTY 0)
(def WALL 1)
(def WATER 2)

(defn generate-walls [w h]
  (let [map (new js/ROT.Map.Cellular w h)]
    (. map randomize 0.25)
    (js->clj (aget map "_map"))))

(defn generate-water [w h]
  (let [map (new js/ROT.Map.Cellular w h)]
    (. map randomize 0.4)
    (. map create nil)
    (. map create nil)
    (js->clj (aget map "_map"))))

(defn on-boundary? [w h x y]
  (or (= x 0)
      (= y 0)
      (= x (dec w))
      (= y (dec h))))

(defn instance []
  (let [dims [22 16]
        [w h] dims
        wall-map (generate-walls w h)
        water-map (generate-water w h)]
    {:id     :world
     :dimensions dims
     :contents (vec (for [x (range 0 w)]
                      (vec (for [y (range 0 h)]
                             (let [wall (get-in wall-map [x y])
                                   water (get-in water-map [x y])]
                               (if (on-boundary? w h x y)
                                 WALL
                                 (cond
                                   (= water 1) WATER
                                   (= wall 1) WALL
                                   :else EMPTY)))))))
     :graphics nil
     :init   (fn [world state]
               (let [{:keys [container] :as world} (assoc world :container (new js/PIXI.Container))
                     [w h] (:dimensions world)
                     contents (:contents world)]


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

(defn get-world [state]
  (let [bgs (:background state)
        world (first bgs)]
    world))

(defn get-tile [state x y]
  (let [world (get-world state)
        contents (:contents world)]
    (get-in contents [x y])))

(defn find-space [state wanted-terrain]
  (let [world (get-world state)
        [w h] (:dimensions world)
        candidates (filter some? (flatten (for [x (range 0 w)]
                                            (for [y (range 0 h)]
                                              (when (= wanted-terrain (get-tile state x y))
                                                {:x x :y y})))))
        pick (rand-nth candidates)]
    [(:x pick) (:y pick)]))