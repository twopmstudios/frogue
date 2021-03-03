(ns pixi.events
  (:require
   [clojure.walk :refer [postwalk]]))

(defn handle-event [state ev]
  (let [state' (update
                state
                :actors
                (fn [actors]
                  (postwalk
                   (fn [node]
                     (if (and (:events node) (get-in node [:events ev]))
                       (let [updated-node ((get-in node [:events ev]) node state)]
                         updated-node)
                       node))
                   actors)))]
    ;; (js/console.log state')
    state'))

(defn trigger-event! [state ev]
  (vswap! state #(handle-event % ev)))
