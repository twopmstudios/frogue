(ns fae.assets)


(def paths
  {:at "assets/at.png"
   :frog "assets/frog.png"
   :other-frog "assets/other-frog.png"
   :empty "assets/empty.png"
   :logo "assets/logo.png"
   :gnat "assets/gnat.png"
   :lilypad "assets/lilypad.png"
   :powerup "assets/powerup.png"
   :mosquito "assets/mosquito.png"
   :skink "assets/skink.png"
   :newt "assets/newt.png"
   :snake "assets/snake.png"
   :dot "assets/dot.png"
   :wall "assets/wall.png"
   :water "assets/water.png"
   :door "assets/door.png"
   :game-over  "assets/game-over.png"
   :win  "assets/win.png"
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
