(ns fae.util
  (:require [goog.string :as gstring]
            [goog.string.format]))

(defn in?
  "true if coll contains elm"
  [coll elm]
  (boolean (some #(= elm %) coll)))

(defn format [s & args]
  (apply gstring/format s args))

(defn defer
  ([f] (defer f 0))
  ([f timeout] (js/setTimeout (fn [] (f)) timeout)))