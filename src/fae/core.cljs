(ns ^:figwheel-hooks fae.core
  (:require
   [fae.assets :as assets]
   [fae.engine :as engine]
   [fae.player :as player]
   [fae.world :as world]
   [fae.fps :as fps]
   [fae.ui :as ui]
   [fae.state :as state]
   [fae.input :as input]
   [fae.print :as print]
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
        (if (and (:transform node) (:graphics node) (:update node))
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
  (let [db state/db]
    (vswap! db assoc :game-state :stopped)
    (engine/clear-stage @db)
    (vswap! db
            (fn [current-state]
              (-> current-state
                  (merge (select-keys (initial-state) [:game-state :background :actors :foreground :vector-field])))))
    (engine/init-scene db)
    (engine/init-render-loop db)
    (vswap! db assoc :game-state :started)
    (.start (:ticker @db))))


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
   :game-state   :stopped
   :update       update!
   :background   [(world/instance)]
   :foreground   []
   :actors       (reverse (concat [(fps/instance state/db [0 0])]
                                  (flatten (for [x (range 0 10)]
                                             (for [y (range 0 10)]
                                               (player/instance state/db [x y]))))))})

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
  [canvas state/db scale dpi resolution])

;; -------------------------
;; Initialize app


(defn mount-root []
  (print/lifecycle "mounted")
  (input/attach-listeners)
  (init-state state/db)
  (r/render [game] (.getElementById js/document "app"))
  (let [s (:stage @state/db)]
    (js/console.log @state/db)
    (. (. s -scale) set (* dpi scale))))

(defn init! []
  (print/lifecycle "init")
  (set! js/PIXI.settings.SCALE_MODE js/PIXI.SCALE_MODES.NEAREST) ;; allows chunky pixels
  (assets/load! mount-root))

(defonce app (init!))

(defn ^:before-load my-before-reload-callback []
  (input/clear-listeners))

(defn ^:after-load my-after-reload-callback []

  (mount-root))