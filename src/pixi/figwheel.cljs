(ns ^:figwheel-hooks pixi.figwheel)

(defn ^:before-load my-before-reload-callback []
  (println "BEFORE reload!!!"))

(defn ^:after-load my-after-reload-callback []
  (println "AFTER reload!!!"))