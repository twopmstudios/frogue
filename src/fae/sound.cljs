(ns fae.sound
  (:require [fae.print :as print]))

(def sounds
  {:boing "assets/boing.wav"
   :croak "assets/croak.ogg"
   :lick "assets/lick.mp3"
   :death "assets/death.mp3"
   :eggs "assets/eggs.mp3"
   :door "assets/door.mp3"
   :slap "assets/slap.mp3"
   :step "assets/step.mp3"
   :powerup "assets/powerup.mp3"
   :title "assets/music/03-title.mp3"
   :title-intro "assets/music/03-title-intro.mp3"
   :game-over "assets/music/04-game-over.mp3"
   :game-over-intro "assets/music/04-game-over-intro.mp3"
   :win "assets/music/01-win.mp3"
   :win-intro "assets/music/01-win-intro.mp3"
   :game "assets/music/02-game.mp3"})

(def muted (volatile! false))

(def music [:title :title-intro :game-over :game-over-intro :win :win-intro :game])

(defn play! [key keep-looping]
  (let [sound js/PIXI.sound]
    (. sound play (name key) #js {:loop keep-looping})))

(defn toggle-mute! []
  (vswap! muted not)
  (let [new-muted @muted
        sound js/PIXI.sound]
    (if new-muted
      (. sound muteAll)
      (. sound unmuteAll))))

(defn stop! [key]
  (let [sound js/PIXI.sound]
    (. sound stop (name key))))

(defn change-music! [to-play]
  (doseq [t music]
    (stop! t))
  (play! to-play true))

(defn play-music-with-intro! [intro to-play]
  (doseq [t music]
    (stop! t))
  (let [sound js/PIXI.sound]
    (. sound play (name intro) #js {:complete (fn [] (change-music! to-play))})))

(defn register! []
  (let [paths (into [] sounds)
        sound js/PIXI.sound]
    (doseq [[key path] paths]
      (. sound add (name key) #js {:src path
                                   :preload true}))))
