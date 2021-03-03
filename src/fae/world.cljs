(ns fae.world
  (:require
   [fae.engine :as engine]
   [fae.grid :as grid]
   [fae.physics :refer [gravitational-acceleration-at-point nearest-point]]))

(defn world-icon []
  (doto (js/PIXI.Graphics.)
    (.beginFill 0x3355ff 0.5)
    (.lineStyle 3 0xFF5500)
    (.moveTo -12.5 -10)
    (.lineTo 12.5 0)
    (.lineTo -12.5 10)
    (.lineTo -12.5 -10)
    (.endFill)))

(defn instance []
  {:id     "world"
   :init   (fn [vector-field state]
             (let [{:keys [container] :as vector-field} (assoc vector-field :container (js/PIXI.Container.))]
               (doseq [x (range 0 (/ (:width state) grid/size))]
                 (doseq [y (range 0 (/ (:height state) grid/size))]
                   (let [s (engine/sprite "dot.png" [0 0])]
                     (.addChild container s)
                     (set! (.-x s) (* x grid/size))
                     (set! (.-y s) (* y grid/size)))))
              ;;  (set! (.-width s) (:width state))
              ;;  (set! (.-height s) (:height state))

               (.addChild (:stage state) container)
               vector-field))
   :update (fn [vector-field state])})

;; (defn instance [state]
;;   {:id       :world
;;    :type     :world
;;    :graphics (engine/sprite "ship.gif")
;;    :z-index  1
;;    :width    25
;;    :height   20
;;    :init     (fn [ship state]
;;                (assoc ship
;;                       :x 128
;;                       :y 128))
;;    :update (fn [])})
