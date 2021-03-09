(ns fae.game
  (:require
   [fae.engine :as engine]
   [fae.entities.gnat :as gnat]
   [fae.entities.mosquito :as mosquito]
   [fae.entities.skink :as skink]
   [fae.entities.newt :as newt]
   [fae.entities.snake :as snake]
   [fae.entities.door :as door]
   [fae.entities :as entities]
   [fae.world :as world]
   [fae.util :as util]
   [fae.state :as state]
   [fae.systems :as sys]
   [fae.print :as print]))

(declare start!)
(declare restart!)
(declare update!)

(defn start! [state]
  (print/lifecycle "start!")
  (doseq [to-spawn (:to-spawn state)]
    (entities/add-entity! (case to-spawn
                            :door (door/instance state (world/find-space state world/DOOR))
                            :gnat (gnat/instance state (world/find-space state world/WATER))
                            :mosquito (mosquito/instance state (world/find-space state world/WATER))
                            :skink (skink/instance state (world/find-space state world/EMPTY))
                            :snake (snake/instance state (world/find-space state world/EMPTY))
                            :newt (newt/instance state (world/find-space state world/EMPTY)))))
  state)

(defn restart! []
  (print/lifecycle "restart!")
  (let [db state/db]
    (vswap! db assoc :game-state :stopped)
    (engine/clear-stage @db)
    (vswap! db
            (fn [current-state]
              (-> current-state
                  (merge (select-keys (state/initial-state) [:game-state :background :actors :foreground])))))
    (engine/init-scene db)
    (engine/cancel-render-loop db)
    (vswap! db assoc :ticker (new js/PIXI.Ticker))
    (engine/init-render-loop db update!)
    (vswap! db assoc :game-state :started)
    (.start (:ticker @db))
    (vswap! db start!)))

(defn event-hook [state ev _data]
  (case ev
    :door-entered (do (util/defer restart!)
                      state)
    :progress-event (assoc-in state [:progress :some-value] true)
    state))

(defn update-actors [state]
  (let [state' (sys/execute-events state event-hook)
        state'' (reduce (fn [state' state-sys] (sys/execute-state state' state-sys)) state' sys/state)]
    (doseq [effect-sys sys/effect]
      (sys/execute-effect state effect-sys))

    state''))

(defn update-scene-objects [state]
  (doseq [{:keys [update] :as object} (into (:background state) (:foreground state))]
    (update object state)))

(defn update! [state]
  (when (engine/started? state)
    (let [to-remove @entities/to-remove
          to-add @entities/to-add]

      (when (> (count to-add) 0)
        (vreset! entities/to-add []))

      (when (> (count to-remove) 0)
        (println "removing" to-remove)
        (doseq [id to-remove]
          (when-let [ent (entities/get-by-id id state)]
            (println "remove" ent)
            (engine/remove-actor-from-stage (:stage state) ent)))

        (vreset! entities/to-remove []))

      (-> state
          (entities/remove-actors to-remove)
          (entities/add-actors to-add)
          (update-actors)))))