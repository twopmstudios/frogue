(ns fae.behavior.damaged
  (:require [fae.entities :as entities]))

(defn handle [g amount]
  (let [new-hp (- (get-in g [:stats :hp]) amount)]
    (when (<= new-hp 0)
      (entities/remove-entity (:id g)))
    (assoc-in g [:stats :hp] new-hp)))