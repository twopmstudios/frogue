(ns fae.grid)

(def size 16)
(defn snap-to-grid
  ([n] (* size (int (/ n size))))
  ([x y] [(snap-to-grid x) (snap-to-grid y)]))
