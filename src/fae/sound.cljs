(ns fae.sound
  (:require [fae.print :as print]))

(def sounds
  {:boing "assets/boing.wav"})

(defn play! [key]
  (let [sound js/PIXI.sound]
    (. sound play (name key))))

(defn register! []
  (let [paths (into [] sounds)
        sound js/PIXI.sound]
    (doseq [[key path] paths]
      (. sound add (name key) #js {:src path
                                   :preload true}))))
