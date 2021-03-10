(ns fae.entities.door
  (:require
   [fae.engine :as engine]
   [fae.events :as events]
   [fae.entities :as entities]
   [fae.world :as world]
   [fae.behavior.id :as id]
   [fae.behavior.movement :as move]
   [fae.behavior.standard :as standard]
   [fae.grid :as grid]))

(defn init! [pos p _state] (move/set-initial-position p pos))

(defn update! [p _state] (move/smooth-move p))

(defn build-sprite []
  (let [spr (engine/sprite "door.png" [0 0])]
    spr))

(defn invert-side [side]
  (case side
    :top :bottom
    :bottom :top
    :left :right
    :right :left
    nil))

(defn handle-bumped [door state {bumpee :bumpee
                                 bumper-id :bumper}]
  (let [came-from (get-in state [:progress :came-from])
        bumper (entities/get-by-id bumper-id state)]
    (if (and (= :player (:type bumper))
             (= bumpee (:id door)))
      (if (not (:locked door))

        (if (not= came-from (invert-side (:side door))) ;; prevent player leaving through the door they entered through 
          (do (events/trigger-event! :door-entered {:side (:side door)})
              door)
          (do (events/trigger-event! :log-entry-posted {:msg "In this life we can only go forward."})
              door))

        (do (events/trigger-event! :log-entry-posted {:msg "You can't leave just yet..."})
            door))
      door)))

(defn determine-side [x y [w h]]
  (cond
    (= x (dec w)) :right
    (= x 0) :left
    (= y (dec h)) :bottom
    (= y 0) :top
    :else nil))

(defn instance [_state [x y]]
  {:id       (id/generate!)
   :type     :door
   :side     (determine-side x y world/DIMENSIONS)

   :transform {:position {:x 0 :y 0}
               :rotation 0}

   :stats {}

   :traits [:flying]
   :effects [:damage
             :tire]
   :status []

   :grid {:x 0 :y 0}
   :graphics (build-sprite)
   :z-index  1

   :locked false

   :inbox []
   :events {:bump handle-bumped
            :doors-locked (fn [door _state _data] (assoc door :locked true))
            :doors-unlocked (fn [door _state _data] (assoc door :locked false))}
   :init   (partial init! [x y])
   :update update!})