(ns fae.entities.gnat
  (:require
   [fae.engine :as engine]
   [fae.print :as print]
   [fae.events :as e]
   [fae.entities :as entities]
   [fae.behavior.id :as id]
   [fae.behavior.movement :as move]
   [fae.grid :as grid]))

(defn move [{{:keys [position]} :transform :as player} x y]
  (-> player
      (assoc-in [:transform :position :x] (+ (:x position) x))
      (assoc-in [:transform :position :y] (+ (:y position) y))))

(defn move-grid [{:keys [grid] :as player} x y]
  (-> player
      (assoc-in [:grid :x] (+ (:x grid) x))
      (assoc-in [:grid :y] (+ (:y grid) y))))

(defn init! [[x y] p _state]
  (println "init gnat" x y (:id p))
  (-> p
      (assoc-in [:grid :x] x)
      (assoc-in [:grid :y] y)
      (assoc-in [:transform :position :x] (* x grid/size))
      (assoc-in [:transform :position :y] (* y grid/size))))

(defn update! [p _state]
  (let [target-x (* grid/size (get-in p [:grid :x]))
        target-y (* grid/size (get-in p [:grid :y]))
        curr-x (get-in p [:transform :position :x])
        curr-y (get-in p [:transform :position :y])
        x (+ curr-x (/ (- target-x curr-x) 5))
        y (+ curr-y (/ (- target-y curr-y) 5))]
    (-> p
        (assoc-in [:transform :position :x] x)
        (assoc-in [:transform :position :y] y))))

(defn build-sprite []
  (engine/sprite "gnat.png" [0 0]))

(defn handle-movement [g _state movement]
  (if (> movement 0)
    (update-in g [:grid :x] (fn [x] (+ x movement)))
    g))

(defn handle-lick [g dmg]
  (let [hp (get-in g [:stats :hp])
        new-hp (- hp dmg)]

    (when (<= new-hp 0)
      (entities/remove-entity (:id g)))

    (println "hurt" hp new-hp)
    (assoc-in g [:stats :hp] new-hp)))

(defn instance [_state [x y]]
  {:id       (id/generate!)
   :type     :gnat

   :transform {:position {:x 0 :y 0}
               :rotation 0}

   :stats {:hp 5
           :speed 0.7}
   :movement {:meter 0
              :move-fn handle-movement}

   :grid {:x 0 :y 0}
   :graphics (build-sprite)
   :z-index  1

   :inbox []
   :events {:move-tick (fn [g state] (move/perform g state (get-in g [:movement :move-fn])))
            :licked-target (fn [g state {target-id :id
                                         dmg :dmg}]
                             (if (= (:id g) target-id)
                               (handle-lick g dmg)
                               g))}
   :init   (partial init! [x y])
   :update update!})
