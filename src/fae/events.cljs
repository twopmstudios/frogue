(ns fae.events
  (:require [fae.print :as print]))

(def inbox (volatile! []))

(defn clear-inbox! []
  (vreset! inbox []))

(defn handle-event [_state ev]
  (vswap! inbox (fn [i] (conj i ev))))

(defn trigger-event! [state ev]
  (print/debug (str "trigger:" ev))
  (handle-event state ev))

