(ns ^:figwheel-hooks fae.core
  (:require
   [fae.assets :as assets]
   [fae.engine :as engine]
   [fae.entities.player :as player]
   [fae.entities.gnat :as gnat]
   [fae.entities.mosquito :as mosquito]
   [fae.entities.skink :as skink]
   [fae.entities.fps :as fps]
   [fae.entities.game-log :as game-log]
   [fae.entities :as entities]
   [fae.world :as world]
   [fae.ui :as ui]
   [fae.state :as state]
   [fae.systems :as sys]
   [fae.events :as events]
   [fae.input :as input]
   [fae.print :as print]
   [fae.sound :as sound]
   [fae.util :as util]
   [reagent.core :as r]
   [cljsjs.pixi]
   [cljsjs.pixi-sound]))

(defn update-actors [state]
  (let [state' (sys/execute-events state @events/inbox)
        state'' (reduce (fn [state' state-sys] (sys/execute-state state' state-sys)) state' sys/state)]
    (doseq [effect-sys sys/effect]
      (sys/execute-effect state effect-sys))

    state''))

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


(defn remove-actors [state to-remove]
  (update state :actors (partial filter (fn [a] (not (util/in? to-remove (:id a)))))))

(defn add-actors [state to-add]
  (let [added (map (fn [a] (engine/add-actor-to-stage state a)) to-add)]
    (update state :actors (fn [a] (concat a added)))))

(defn update! [state]
  (when (engine/started? state)
    (let [to-remove @entities/to-remove
          to-add @entities/to-add]

      (when (> (count to-add) 0)
        (vreset! entities/to-add []))

      (when (> (count to-remove) 0)
        (println "removing" to-remove)
        (doseq [id to-remove]
          (when-let [ent (entities/get-by-id id)]
            (println "remove" ent)
            (engine/remove-actor-from-stage (:stage state) ent)))

        (vreset! entities/to-remove []))

      (-> state
          (remove-actors to-remove)
          (add-actors to-add)
          (update-actors)))))

;; init

(def dpi (or js/window.devicePixelRatio 1))
(def scale 2)
;; (def resolution [(.-innerWidth js/window) (.-innerHeight js/window)])
(def resolution [960 540])

(defn initial-state []
  {:score        0
   :cancel-render false
   :game-state   :stopped
   :update       update!
   :background   [(world/instance)]
   :foreground   []
   :actors       [(fps/instance state/db [0 0])
                  (game-log/instance state/db [340 210])
                  (player/instance state/db [10 10])
                  (mosquito/instance state/db [7 2])
                  (mosquito/instance state/db [12 4])
                  (mosquito/instance state/db [2 8])
                  (skink/instance state/db [12 12])
                  (skink/instance state/db [6 2])
                  (gnat/instance state/db [8 2])
                  (gnat/instance state/db [9 3])
                  (gnat/instance state/db [3 3])]})

(defn init-state [state]
  (vreset! state (initial-state))
  (vreset! entities/to-remove [])
  (vreset! entities/to-add [])
  (vreset! events/inbox []))

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
  (assets/load! mount-root)
  (sound/register!))

(defonce app (init!))

(defn ^:before-load my-before-reload-callback []
  (input/clear-listeners)
  (engine/cancel-render-loop state/db))

(defn ^:after-load my-after-reload-callback []
  (mount-root))