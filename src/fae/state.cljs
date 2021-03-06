(ns fae.state)

(def db (volatile! nil))

(defn update-state! [f]
  (vswap! db f))