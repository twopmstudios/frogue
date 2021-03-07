(ns fae.behavior.licked
  (:require [fae.entities :as entities]
            [fae.events :as events]))

(defn handle [g dmg]
  (events/trigger-event! :damaged {:id (:id g) :amount dmg :source "lick"})
  g)