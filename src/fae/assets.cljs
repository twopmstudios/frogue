(ns fae.assets)


(def paths
  {:at "assets/at.png"
   :gnat "assets/gnat.png"
   :dot "assets/dot.png"
   :ship  "assets/ship.gif"
   :fnt "assets/fonts/04b03.ttf.woff"
   :fnt-1 "assets/fonts/04b03.ttf.svg#04b03"
   :fnt-2 "assets/fonts/04b03.ttf.eot"
   :fnt-3 "assets/fonts/04b03.ttf.eot?#iefix"})

(defn load!
  "Loads all files in assets.paths and calls done on completion"
  [done]
  (let [paths (map (fn [[_ v]] v) (into [] paths))
        loader (new js/PIXI.Loader)]
    (doseq [p paths]
      (. loader add p))
    (. loader load done)))
