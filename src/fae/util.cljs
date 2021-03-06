(ns fae.util)

(defn in?
  "true if coll contains elm"
  [coll elm]
  (boolean (some #(= elm %) coll)))
