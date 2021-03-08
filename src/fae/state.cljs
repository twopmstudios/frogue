(ns fae.state
  (:require
   [fae.entities.player :as player]
   [fae.entities.fps :as fps]
   [fae.entities.hud :as hud]
   [fae.entities.game-log :as game-log]
   [fae.world :as world]
   [fae.entities :as entities]
   [fae.events :as events]))

(def db (volatile! nil))

(defn update-state! [f]
  (vswap! db f))

(defn initial-state []
  {:score        0
   :cancel-render false
   :game-state   :stopped
   :background   [(world/instance)]
   :foreground   []
   :to-spawn [:mosquito
              :mosquito
              :mosquito
              :gnat
              :gnat
              :gnat
              :skink
              :skink
              :snake
              :newt]
   :actors       [;;(fps/instance db [440 0])
                  (hud/instance db [356 0])
                  (game-log/instance db [356 210])
                  (player/instance db [10 10])]})


(defn init! [start-fn]
  (vreset! db (initial-state))
  (vreset! entities/to-remove [])
  (vreset! entities/to-add [])
  (events/clear-inbox!)
  (vswap! db start-fn))