(ns fae.entities.fps
  (:require
   [fae.engine :as engine]
   [fae.print :as print]
   [fae.ui :as ui]
   [fae.behavior.id :as id]
   [fae.grid :as grid]))

(defn ship-icon []
  (doto (new js/PIXI.Graphics)
    (.beginFill 0x3355ff 0.5)
    (.lineStyle 3 0xFF5500)
    (.moveTo -12.5 -10)
    (.lineTo 12.5 0)
    (.lineTo -12.5 10)
    (.lineTo -12.5 -10)
    (.endFill)))

(defn update! [{text :graphics :as p} {ticker :ticker}]
  (set! (.-text text) (str (-> (aget ticker "FPS")
                               (.toFixed 2))
                           " FPS"))
  p)

(defn instance [_state [x y]]
  {:id       (id/generate!)
   :type     :fps
   :transform {:position {:x x :y y}}
   :graphics (ui/text-field "FPS" 8 "04b03")
   :z-index  1

   :events {}
   :init     (fn [p _state] p)
   :update update!})
