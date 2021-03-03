(ns fae.ship
  (:require
   [fae.engine :as engine]
   [fae.grid :as grid]))

(defn ship-icon []
  (doto (js/PIXI.Graphics.)
    (.beginFill 0x3355ff 0.5)
    (.lineStyle 3 0xFF5500)
    (.moveTo -12.5 -10)
    (.lineTo 12.5 0)
    (.lineTo -12.5 10)
    (.lineTo -12.5 -10)
    (.endFill)))

(defn instance [state]
  {:id       :ship
   :type     :player
   :graphics (engine/sprite "at.png" [0 0])
   :z-index  1
   :velocity {:y 0 :x 0}
  ;;  :width    25
  ;;  :height   20
   :radius   10
   :mass     35
   :init     (fn [ship state]
               (assoc ship
                      :x (grid/snap-to-grid (/ (:width state) 2))
                      :y (grid/snap-to-grid (/ (:height state) 2))))
   :events {:move-up-pressed (fn [ship _state] (assoc ship :y (- (:y ship) grid/size)))
            :move-down-pressed (fn [ship _state] (assoc ship :y (+ (:y ship) grid/size)))
            :move-right-pressed (fn [ship _state] (assoc ship :x (+ (:x ship) grid/size)))
            :move-left-pressed (fn [ship _state] (assoc ship :x (- (:x ship) grid/size)))}
   :update (fn [ship _] ship)})
