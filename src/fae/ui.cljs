(ns fae.ui
  (:require [fae.engine :as engine]
            [fae.sound :as sound]))

(defn text-field [text size font]
  (-> (new js/PIXI.Text text (new js/PIXI.TextStyle
                                  #js {:fill       "#99e550"
                                       :fontSize   size
                                       :fontFamily font}))))

(defn text-box [{:keys [text x y]}]
  {:x        x
   :y        y
   :graphics (text-field text 30 "Arial")
   :init     (fn [text state] text)
   :update   (fn [])})

(defn button [{:keys [label on-click x y width height]}]
  (let [text   (engine/set-anchor (text-field label 30 "Arial") 0.5 0.5)
        button (doto (new js/PIXI.Graphics)
                 (.addChild (do
                              (set! (.-x text) (/ width 2))
                              (set! (.-y text) (/ height 2))
                              text))
                 (.on "pointerdown" on-click)
                 (.lineStyle 2 0xFF00FF 1)
                 (.beginFill 0xFF00BB 0.25)
                 (.drawRoundedRect 0 0 width height 15)
                 (.endFill))]
    (set! (.-interactive button) true)
    (set! (.-buttonMode button) true)
    {:graphics button
     :x        x
     :y        y
     :init     (fn [{:keys [graphics] :as button} state]
                 (engine/set-graphics-position button)
                 (.addChild (:stage state) graphics)
                 button)
     :update   (fn [])}))

(defn start-button [state parent-graphics]
  (let [graphics (doto (new js/PIXI.Graphics)
                   (.lineStyle 2 0x99e550 1)
                   (.beginFill 0x37946e 0.25)
                   (.drawRoundedRect -10 -3 120 40 15)
                   (.endFill)
                   (.addChild (let [text (text-field "START" 32 "04b03")]
                                (set! (.-x text) 6)
                                (set! (.-y text) (- 2))
                                text))
                   (.on "pointerdown" #(do
                                         (.removeChild (:stage @state) parent-graphics)
                                         (vswap! state assoc :game-state :started)
                                         (engine/add-stage-on-click-event state)
                                         (engine/init-scene state)
                                         (sound/change-music! :game)
                                         (sound/play! :croak false))))]
    (set! (.-interactive graphics) true)
    (set! (.-buttonMode graphics) true)
    graphics))

(defn title-screen [state]
  (let [graphics     (new js/PIXI.Graphics)
        text-lines   ["#7DRL 2021"
                      "Music By aj booker"

                      "Made by TwoPM Studios (Ben Follington & Ricky James)"]
        logo (let [spr (engine/sprite "logo.png")]
               (set! (.-x spr) 240)
               (set! (.-y spr) 64)
               spr)
        frog (let [spr (engine/sprite "frog.png")]
               (set! (.-x spr) 240)
               (set! (.-y spr) 160)
               spr)
        instructions (map-indexed (fn [idx text]
                                    (let [text (text-field text 8 "04b03")]
                                      (set! (.-x text) 10)
                                      (set! (.-y text) (+ 222 (* 10 (inc idx))))
                                      text))
                                  text-lines)
        button       (start-button state graphics)]
    (doseq [line instructions]
      (.addChild graphics line))
    (set! (.-x button) (- (/ (:width @state) 2) 50))
    (set! (.-y button) (+ (/ (:height @state) 2) 40))
    (.addChild graphics button)
    (.addChild graphics frog)
    (.addChild graphics logo)
    (.addChild (:stage @state) graphics)
    state))
