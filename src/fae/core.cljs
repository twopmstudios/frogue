(ns ^:figwheel-hooks fae.core
  (:require
   [fae.assets :as assets]
   [fae.engine :as engine]
   [fae.entities :as entities]
   [fae.game :as game]
   [fae.ui :as ui]
   [fae.state :as state]
   [fae.events :as events]
   [fae.input :as input]
   [fae.print :as print]
   [fae.sound :as sound]
   [reagent.core :as r]
   [cljsjs.pixi]
   [cljsjs.pixi-sound]
   [cljsjs.rot]))

;; init

(def dpi (or js/window.devicePixelRatio 1))
(def scale 2)
;; (def resolution [(.-innerWidth js/window) (.-innerHeight js/window)])
(def resolution [960 540])


(defn canvas [state scale dpi [w h]]
  (let [width w
        height h]
    (r/create-class
     {:component-did-mount
      (engine/init-canvas state (* scale dpi) ui/title-screen game/update!)
      :render
      (fn []
        [:canvas {:width (* width dpi)
                  :height (* height dpi)
                  :style {:width (str width "px")
                          :height (str height "px")}}])})))

(defn game []
  [canvas state/db scale dpi resolution])

;; -------------------------
;; Initialize app

(defn mount-root []
  (print/lifecycle "mounted")
  (input/attach-listeners)
  (state/init! game/start!)
  (r/render [game] (.getElementById js/document "app"))
  (let [s (:stage @state/db)]
    (js/console.log @state/db)
    (. (. s -scale) set (* dpi scale))))

(defn init! []
  (print/lifecycle "init!")
  (set! js/PIXI.settings.SCALE_MODE js/PIXI.SCALE_MODES.NEAREST) ;; allows chunky pixels
  (assets/load! mount-root)
  (sound/register!))

(defonce app (init!))

(defn ^:before-load my-before-reload-callback []
  (input/clear-listeners)
  (engine/cancel-render-loop state/db))

(defn ^:after-load my-after-reload-callback []
  (mount-root))