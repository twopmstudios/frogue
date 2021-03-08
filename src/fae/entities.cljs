(ns fae.entities
  (:require [fae.state :as state]))

(def to-add (volatile! []))
(def to-remove (volatile! []))

(defn get-by-id [id state]
  (let [actors (:actors state)
        matches (filter (fn [node] (= (:id node) id)) actors)]
    (first matches)))

(defn get-by-type [type state]
  (let [actors (:actors state)
        matches (filter (fn [node] (= (:type node) type)) actors)]
    (first matches)))

(defn add-entity! [e]
  ;; add to state vector
  ;; add to stage
  (vswap! to-add (fn [lst] (conj lst e))))

(defn remove-entity [id]
  ;; remove from vector
  ;; remove from stage

  (vswap! to-remove (fn [lst] (conj lst id)))
  (println "entities to remove" @to-remove))
