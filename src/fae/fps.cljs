(ns fae.fps
  (:require
   [fae.engine :as engine]
   [fae.print :as print]
   [fae.ui :as ui]
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

(defn update! [{text :graphics :as p} {ticker :ticker}]
  (set! (.-text text) (.toFixed (.-FPS ticker) 2))
  p)

(defn instance [_state [x y]]
  {:id       :fps
   :type     :fps
   :transform {:position {:x x :y y}}
   :graphics (ui/text-field "FPS" 8 "04b03")
   :z-index  1
   :init     (fn [p _state] p)
   :events {}
   :update update!})
