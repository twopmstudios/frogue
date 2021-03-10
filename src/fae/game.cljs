(ns fae.game
  (:require
   [fae.engine :as engine]
   [fae.entities.gnat :as gnat]
   [fae.entities.mosquito :as mosquito]
   [fae.entities.skink :as skink]
   [fae.entities.newt :as newt]
   [fae.entities.snake :as snake]
   [fae.entities.door :as door]
   [fae.entities.jump-powerup :as jump-powerup]
   [fae.entities.gills-powerup :as gills-powerup]
   [fae.entities.tongue-powerup :as tongue-powerup]
   [fae.entities.other-frog :as other-frog]
   [fae.entities.lilypad :as lilypad]
   [fae.entities :as entities]
   [fae.events :as events]
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

  (let [end-level (util/in? (get state :to-spawn) :end-game)]
    (when (not end-level)
      (doseq [door (world/find-all-spaces state world/DOOR)]
        (entities/add-entity! (door/instance state door)))))

  (doseq [to-spawn (:to-spawn state)]
    (entities/add-entity! (case to-spawn
                            :jump-powerup (jump-powerup/instance state (world/find-space state world/EMPTY))
                            :gills-powerup (gills-powerup/instance state (world/find-space state world/EMPTY))
                            :tongue-powerup (tongue-powerup/instance state (world/find-space state world/EMPTY))
                            :other-frog (other-frog/instance state (world/find-space state world/EMPTY))
                            :lilypad (lilypad/instance state (world/find-space state world/WATER))
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
    (events/clear-inbox!)
    (vswap! db assoc :ticker (new js/PIXI.Ticker))
    (engine/init-render-loop db update!)
    (vswap! db assoc :game-state :started)
    (.start (:ticker @db))
    (vswap! db start!)))

(defn determine-enemies-to-spawn [state]
  (let [room (get-in state [:progress :rooms])]
    (assoc state :to-spawn (case room
                             0 [:gnat :gnat :mosquito :mosquito]
                             1 [:gnat :gnat :gnat :mosquito :mosquito :mosquito]
                             2 [:gnat :skink :gnat :mosquito :jump-powerup] ;; spawn jump
                             3 [:gnat :newt :skink :skink :gnat :mosquito :tongue-powerup] ;; spawn tongue buff
                             4 [:mosquito :mosquito :mosquito :mosquito :skink :skink :skink :newt :gills-powerup] ;; spawn gilles
                             5 [:snake :skink :newt :skink :gnat :gnat :tongue-powerup] ;; spawn tongue buff
                             6 [:gnat :gnat :gnat :other-frog] ;; other frog
                             7 [:snake :snake :snake :snake :gnat] ;; spawn bump buff
                             8 [:skink :newt :skink :newt :skink :mosquito :mosquito :gnat]
                             9 [:newt :newt :newt :newt :mosquito :mosquito :mosquito] ;; spawn bump buff
                             10 [:gnat :gnat :gnat :lilypad] ;; win game
                             []))))

(defn change-level! []
  (print/lifecycle "change-level!")
  (let [db state/db]
    (vswap! db assoc :game-state :stopped)
    (engine/clear-stage @db)
    (vswap! db
            (fn [current-state]
              (-> current-state
                  (merge (select-keys (state/initial-state) [:game-state :background :actors :foreground]))
                  (determine-enemies-to-spawn))))
    (engine/init-scene db)
    (engine/cancel-render-loop db)
    (events/clear-inbox!)
    (vswap! db assoc :ticker (new js/PIXI.Ticker))
    (engine/init-render-loop db update!)
    (vswap! db assoc :game-state :started)
    (.start (:ticker @db))
    (vswap! db start!)))

(defn game-over! []
  (print/lifecycle "game-over!")
  (let [db state/db]
    (vswap! db assoc :game-state :game-over)
    (engine/clear-stage @db)
    (engine/cancel-render-loop db)
    (events/clear-inbox!)
    (vswap! db assoc :ticker (new js/PIXI.Ticker))
    (engine/init-render-loop db update!)
    (.addChild (:stage @db) (let [spr (engine/sprite "game-over.png" [0.5 0.5])]
                              (set! (.-x spr) 240)
                              (set! (.-y spr) 135)
                              spr))))

(defn game-won! []
  (print/lifecycle "game-won!")
  (let [db state/db]
    (vswap! db assoc :game-state :game-won)
    (engine/clear-stage @db)
    (engine/cancel-render-loop db)
    (events/clear-inbox!)
    (vswap! db assoc :ticker (new js/PIXI.Ticker))
    (engine/init-render-loop db update!)
    (.addChild (:stage @db) (let [spr (engine/sprite "win.png" [0.5 0.5])]
                              (set! (.-x spr) 240)
                              (set! (.-y spr) 135)
                              spr))))

(defn event-hook [state ev data]
  (case ev
    :door-entered (do
                    (util/defer change-level!)
                    (-> state
                        (update-in [:progress :rooms] inc)
                        (assoc-in [:progress :came-from] (:side data))))
    :win-game (do (util/defer game-won!)
                  state)
    :player-dead (do (util/defer game-over!)
                     state)
    :eggs-fertilized (do
                       (events/trigger-event! :doors-unlocked)
                       (assoc-in state [:progress :fertilzed] true))
    :powerup-get (case (:kind data)
                   :jump (assoc-in state [:progress :jump] true)
                   :gills (assoc-in state [:progress :gills] true)
                   :tongue-length state) ;; TODO: think about how to persist stat upgrades across levels
    :progress-event (assoc-in state [:progress :gills] true)
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