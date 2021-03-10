(ns fae.entities.hud
  (:require
   [fae.engine :as engine]
   [fae.print :as print]
   [fae.entities :as entities]
   [fae.ui :as ui]
   [fae.behavior.id :as id]
   [fae.grid :as grid]))

(defn update! [{stats :stats :as hud} state]
  (let [player (entities/get-by-type :player state)

        size-txt (:size stats)
        size-val (get-in player [:stats :size])

        eggs-txt (:eggs stats)
        eggs-val (get-in player [:stats :eggs])

        lick-txt (:lick stats)
        lick-val (get-in player [:stats :lick])

        tongue-txt (:tongue stats)
        tongue-val (get-in player [:stats :tongue])

        poisonous-txt (:poisonous stats)
        poisonous-val (get-in player [:stats :poisonous])]
    (set! (.-text size-txt) (str "SIZE " size-val))
    (set! (.-text eggs-txt) (str "EGGS " eggs-val))
    (set! (.-text lick-txt) (str "LICK " lick-val))
    (set! (.-text tongue-txt) (str "TONGUE " tongue-val))
    (set! (.-text poisonous-txt) (str "POISON " poisonous-val))
    hud))

(defn set-position [gfx x y]
  (set! (.-x gfx) x)
  (set! (.-y gfx) y)
  gfx)

(defn instance [_state [x y]]
  (let [stats {:size (set-position (ui/text-field "HUD" 8 "04b03") 0 0)
               :eggs (set-position (ui/text-field "HUD" 8 "04b03") 0 10)
               :lick (set-position (ui/text-field "HUD" 8 "04b03") 0 20)
               :tongue (set-position (ui/text-field "HUD" 8 "04b03") 0 30)
               :poisonous (set-position (ui/text-field "HUD" 8 "04b03") 0 40)}]
    {:id       (id/generate!)
     :type     :fps
     :transform {:position {:x x :y y}}
     :stats stats
     :graphics (let [container (new js/PIXI.Container)]
                 (doseq [[_ v] stats]
                   (.addChild container v))
                 (.addChild container (set-position (engine/sprite "frog.png") 80 24))
                 container)
     :z-index  1

     :events {}
     :init     (fn [p _state] p)
     :update update!}))
