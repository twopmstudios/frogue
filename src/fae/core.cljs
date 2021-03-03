(ns ^:figwheel-hooks fae.core
  (:require
   [fae.assets :as assets]
   [fae.engine :as engine]
   [fae.force-field :as force-field]
   [fae.ship :as ship]
   [fae.world :as world]
   [fae.ui :as ui]
   [fae.state :as state]
   [fae.input :as input]
   [clojure.walk :refer [postwalk]]
   [reagent.core :as r]
   [cljsjs.pixi]
   [cljsjs.pixi-sound]))

(defn update-actors [state]
  (update
   state
   :actors
   (fn [actors]
     (postwalk
      (fn [node]
        (if (and (:graphics node) (:update node))
          (let [updated-node ((:update node) node state)]
            (engine/set-graphics-position updated-node)
            updated-node)
          node))
      actors))))

(defn update-scene-objects [state]
  (doseq [{:keys [update] :as object} (into (:background state) (:foreground state))]
    (update object state)))

;;todo only run when adding/removing actors
(defn group-actors-by-type [actors]
  (reduce
   (fn [entities {:keys [type] :as actor}]
     (case type
       :player (assoc entities :player actor)
       :deathzone (update entities :deathzones (fnil conj []) actor)
       :prize (update entities :prizes (fnil conj []) actor)
       entities))
   {}
   actors))

;; lifecycle

(declare initial-state)

(defn restart! []
  (vswap! state/state assoc :game-state :stopped)
  (engine/clear-stage @state/state)
  (vswap! state/state
          (fn [current-state]
            (-> current-state
                (merge (select-keys (initial-state) [:game-state :background :actors :foreground :vector-field])))))
  (engine/init-scene state/state)
  (engine/init-render-loop state/state)
  (vswap! state/state assoc :game-state :started)
  (.start (:ticker @state/state)))


(defn update! [state]
  (when (engine/started? state)
    (-> state
        (update-actors))))



;; init

(def dpi (or js/window.devicePixelRatio 1))
(def scale 3)
;; (def resolution [(.-innerWidth js/window) (.-innerHeight js/window)])
(def resolution [960 540])

(defn initial-state []
  {:score        0
   :total-prizes 5
   :game-state   :stopped
   :vector-field nil
   :force-radius 20
   :update       update!
   :background   [(force-field/instance) (world/instance)]
   :foreground   []
   :actors       [(ship/instance state/state)]})

(defn init-state [state]
  (vreset! state (initial-state)))

(defn canvas [state scale dpi [w h]]
  (let [width w
        height h]
    (r/create-class
     {:component-did-mount
      (engine/init-canvas state (* scale dpi) ui/help-menu)
      :render
      (fn []
        [:canvas {:width (* width dpi)
                  :height (* height dpi)
                  :style {:width (str width "px")
                          :height (str height "px")}}])})))

(defn game []
  [canvas state/state scale dpi resolution])

;; -------------------------
;; Initialize app


(defn mount-root []
  (println "! mounted")
  (input/attach-listeners)
  (init-state state/state)
  (r/render [game] (.getElementById js/document "app"))
  (let [s (:stage @state/state)]
    (js/console.log @state/state)
    (. (. s -scale) set scale)))

(defn init! []
  (set! js/PIXI.settings.SCALE_MODE js/PIXI.SCALE_MODES.NEAREST)
  (assets/load! mount-root))

(defonce app (init!))

(defn ^:before-load my-before-reload-callback []
  (input/clear-listeners))

(defn ^:after-load my-after-reload-callback []

  (mount-root))