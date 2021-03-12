(ns fae.entities.game-log
  (:require
   [clojure.string :as str]
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

(defn update! [{text :graphics log :log :as p} _state]
  (let [lines (str/join "\n" log)]
    (set! (.-text text) lines))
  p)

(defn instance [_state [x y]]
  {:id       (id/generate!)
   :type     :fps
   :transform {:position {:x x :y y}}
   :log []
   :graphics (ui/text-field "" 8 "04b03")
   :z-index  1

   :events {:log-entry-posted (fn [p _state {msg :msg}]
                                (update p :log (fn [log] (vec (take-last 8 (conj log msg))))))}
   :init     (fn [p _state] p)
   :update update!})
