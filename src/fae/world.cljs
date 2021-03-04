(ns fae.world
  (:require
   [fae.engine :as engine]
   [fae.grid :as grid]))

(defn instance []
  {:id     "world"
   :init   (fn [world state]
             (let [{:keys [container] :as world} (assoc world :container (new js/PIXI.Container))]
               (doseq [x (range 0 (/ (:width state) grid/size))]
                 (doseq [y (range 0 (/ (:height state) grid/size))]
                   (let [s (engine/sprite "dot.png" [0 0])]
                     (.addChild container s)
                     (set! (.-x s) (* x grid/size))
                     (set! (.-y s) (* y grid/size)))))

               (.addChild (:stage state) container)
               world))
   :update (fn [world state])})
