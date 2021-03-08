(ns fae.systems
  (:require [fae.engine :as engine]
            [fae.events :as events]
            [fae.util :as util]
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

(def BAILOUT 100)

(defn execute-events [state hook-fn]
  ;; don't be scared, this is a little gnarly looking
  ;; in our game processing events can yield MORE events
  ;; so after processing the queue, check if it has refilled
  ;; if it had, process again
  ;;   
  ;; keep going until either the queue is empty or BAILOUT attempts occur

  (loop [events @events/inbox
         counter 0]
    (when (> (count events) 0)
      (println (util/format "pass %i: processing events" counter) events))

    (events/clear-inbox!)
    (let [state' (update state :actors
                         (fn [actors]
                           (map (fn [node] (event-processing-system node state events))
                                actors)))
          state'' (reduce (fn [s [ev data]] (hook-fn s ev data)) state' events)]
      (if (and (> (count @events/inbox) 0)
               (< counter BAILOUT))
        (recur @events/inbox (inc counter))
        (do
          (when (>= counter BAILOUT)
            (js/console.error (util/format "Event queue failed to empty after %i attempts" BAILOUT)))
          state'')))))

(defn execute-state [state system]
  (update state :actors
          (fn [actors]
            (map (fn [node] (system node state))
                 actors))))

(defn execute-effect [state system]
  (doseq [node (:actors state)]
    (system node state)))
