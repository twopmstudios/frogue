(ns fae.world
  (:require
   [fae.engine :as engine]
   [fae.util :as util]
   [fae.grid :as grid]))

(def EMPTY 0)
(def WALL 1)
(def WATER 2)
(def DOOR 3)

(def DIMENSIONS [18 14])
(def MAX-LEVEL 10)

(defn generate-walls [w h has-jump]
  (let [map (new js/ROT.Map.Cellular w h)]
    (. map randomize (if has-jump 0.25 0.15))
    (js->clj (aget map "_map"))))

(defn generate-water [w h has-gills]
  (let [map (new js/ROT.Map.Cellular w h)]
    (. map randomize (if has-gills 0.4 0.3))
    (when has-gills (. map create nil))
    (. map create nil)
    (js->clj (aget map "_map"))))

(defn on-boundary? [w h x y]
  (or (= x 0)
      (= y 0)
      (= x (dec w))
      (= y (dec h))))

(defn instance [state]
  (let [num-rooms (or (get-in state [:progress :rooms]) 0)
        has-jump (or (get-in state [:progress :jump]) false)
        has-gills (or (get-in state [:progress :gills]) false)
        [w h] DIMENSIONS
        wall-map (generate-walls w h has-jump)
        water-map (generate-water w h has-gills)]

    {:id     :world
     :dimensions DIMENSIONS
     :contents (vec (for [x (range 0 w)]
                      (vec (for [y (range 0 h)]
                             (let [wall (get-in wall-map [x y])
                                   water (get-in water-map [x y])]
                               (cond
                                 (and (on-boundary? w h x y)
                                      (or (= x (/ w 2))
                                          (= y (/ h 2)))
                                      (not= num-rooms MAX-LEVEL)) DOOR
                                 (on-boundary? w h x y) WALL
                                 (= water 1) WATER
                                 (= wall 1) WALL
                                 :else EMPTY))))))
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
                               2 (engine/sprite "water.png" [0 0])
                               3 (engine/sprite "empty.png" [0 0]))]
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

(defn find-all-spaces [state wanted-terrain]
  (let [world (get-world state)
        [w h] (:dimensions world)
        candidates (filter some? (flatten (for [x (range 0 w)]
                                            (for [y (range 0 h)]
                                              (when (= wanted-terrain (get-tile state x y))
                                                {:x x :y y})))))]
    (map (fn [{:keys [x y]}] [x y]) candidates)))

(defn find-space [state wanted-terrain]
  (let [candidates (find-all-spaces state wanted-terrain)]
    (if (> (count candidates) 0)
      (rand-nth candidates)
      (do
        (js/console.error "Could not find any spaces of terrain" wanted-terrain)
        nil))))
