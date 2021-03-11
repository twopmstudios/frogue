(ns fae.state
  (:require
   [fae.entities.hud :as hud]
   [fae.entities.game-log :as game-log]
   [fae.entities :as entities]
   [fae.events :as events]))

(def db (volatile! nil))

(defn update-state! [f]
  (vswap! db f))

(defn initial-state []
  {:score         0
   :cancel-render false
   :game-state    :stopped
   :scene         :default
   :progress {:jump false
              :gills false
              :fertilzed false
              :rooms 0
              :came-from nil}
   :background   []
   :foreground   []
   :to-spawn [:mosquito
              :gnat
              :gnat]
   :actors       [;;(fps/instance db [440 0])
                  (hud/instance @db [322 0])
                  (game-log/instance @db [322 180])]})


(defn init! [start-fn]
  (vreset! db (initial-state))
  (vreset! entities/to-remove [])
  (vreset! entities/to-add [])
  (events/clear-inbox!)
  (vswap! db start-fn))