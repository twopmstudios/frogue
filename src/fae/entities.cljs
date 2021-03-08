(ns fae.entities
  (:require [fae.util :as util]
            [fae.engine :as engine]))

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
  (vswap! to-add (fn [lst] (conj lst e))))

(defn remove-entity [id]
  (vswap! to-remove (fn [lst] (conj lst id))))

(defn remove-actors [state to-remove]
  (update state :actors (partial filter (fn [a] (not (util/in? to-remove (:id a)))))))

(defn add-actors [state to-add]
  (let [added (map (fn [a] (engine/add-actor-to-stage state a)) to-add)]
    (update state :actors (fn [a] (concat a added)))))