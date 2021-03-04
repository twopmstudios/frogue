(ns fae.systems
  (:require [fae.engine :as engine]))

(defn update-system [node state]
  (if (:update node)
    (let [updated-node ((:update node) node state)]
      updated-node)
    node))

(defn rotate-system [node _state]
  (if (:rotate-constantly node)
    (let [rotate-speed (get node :rotate-constantly)
          rotation (get-in node [:transform :rotation])
          rotation' (+ rotation rotate-speed)]
      (-> node
          (assoc-in [:transform :rotation] rotation')))

    node))

(defn graphics-system [node _state]
  (when (and (:transform node) (:graphics node))
    (engine/set-graphics-position node)))

(def state [update-system rotate-system])
(def effect [graphics-system])

(defn execute-state [state system]
  (update state :actors
          (fn [actors]
            (map (fn [node] (system node state))
                 actors))))

(defn execute-effect [state system]
  (doseq [node (:actors state)]
    (system node state)))
