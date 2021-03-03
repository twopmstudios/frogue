(ns fae.assets)


(def paths
  {:at "assets/at.png"
   :dot "assets/dot.png"
   :ship  "assets/ship.gif"})

(defn load!
  "Loads all files in assets.paths and calls done on completion"
  [done]
  (let [paths (map (fn [[_ v]] v) (into [] paths))
        loader js/PIXI.loader]
    (doseq [p paths]
      (. loader add p))
    (. loader load done)))
