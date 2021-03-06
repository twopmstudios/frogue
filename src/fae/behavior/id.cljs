(ns fae.behavior.id)

(def id (volatile! 0))

(defn generate! []
  (let [ret @id]
    (vswap! id inc)
    ret))
