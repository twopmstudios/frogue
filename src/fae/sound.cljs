(ns fae.sound
  (:require [fae.print :as print]))

(def sounds
  {:boing "assets/boing.wav"
   :croak "assets/croak.ogg"
   :lick "assets/lick.mp3"
   :door "assets/door.mp3"
   :slap "assets/slap.mp3"
   :step "assets/step.mp3"
   :powerup "assets/powerup.mp3"
   :title "assets/music/03-title.mp3"
   :game-over "assets/music/04-game-over.mp3"
   :win "assets/music/01-win.mp3"
   :game "assets/music/02-game.mp3"})

(def music [:title :game-over :win :game])

(defn play! [key keep-looping]
  (let [sound js/PIXI.sound]
    (. sound play (name key) #js {:loop keep-looping})))

(defn stop! [key]
  (let [sound js/PIXI.sound]
    (. sound stop (name key))))

(defn change-music! [to-play]
  (doseq [t music]
    (stop! t))
  (play! to-play true))

(defn register! []
  (let [paths (into [] sounds)
        sound js/PIXI.sound]
    (doseq [[key path] paths]
      (. sound add (name key) #js {:src path
                                   :preload true}))))
