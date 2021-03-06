(ns fae.behavior.licked
  (:require [fae.entities :as entities]))

(defn handle [g dmg]
  (let [hp (get-in g [:stats :hp])
        new-hp (- hp dmg)]

    (when (<= new-hp 0)
      (entities/remove-entity (:id g)))

    (println "hurt" hp new-hp)
    (assoc-in g [:stats :hp] new-hp)))