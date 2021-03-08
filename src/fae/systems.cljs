(ns fae.systems
  (:require [fae.engine :as engine]
            [fae.events :as events]
            [fae.print :as print]))

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

(defn event-processing-system [node state events]
  ;; (println "node->events" (:events node))
  ;; (println "node->inbox" (:inbox node))
  ;;   

  (let [node' (reduce (fn [acc [ev data]]
                        (if-let [handler (get-in acc [:events ev])]
                          (handler acc state data)
                          acc))
                      node
                      events)]

    node'))

(defn graphics-system [node _state]
  (when (and (:transform node) (:graphics node))
    (engine/set-graphics-position node)))

(def state [update-system rotate-system])
(def effect [graphics-system])

(defn execute-events [state]
  (let [events @events/inbox]
    ;; (when (> (count events) 0)
    ;;   (println "processing events" events))
    (events/clear-inbox!)
    (update state :actors
            (fn [actors]
              (map (fn [node] (event-processing-system node state events))
                   actors)))))

(defn execute-state [state system]
  (update state :actors
          (fn [actors]
            (map (fn [node] (system node state))
                 actors))))

(defn execute-effect [state system]
  (doseq [node (:actors state)]
    (system node state)))
