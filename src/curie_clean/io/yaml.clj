(ns curie-clean.io.yaml
    (:require [clj-yaml.core :as yaml]
              [clojure.walk :as walk]
              [flatland.ordered.map :as omap])
    (:gen-class))


(defn read-yaml [path]
  (yaml/parse-string (slurp path)))


(def OrderedMapClass (class (omap/ordered-map)))

(defn force-regular-map 
  "Converts an ordered map to a regular map"
  [yaml-map]
  (walk/postwalk
   (fn [x]
     (if (instance? OrderedMapClass x)
       (into {} x)
       x))
   yaml-map))


(defn- deep-merge 
  "Merge nested maps"
  [a b]
  (cond
    (and (map? a) (map? b)) (merge-with deep-merge a b)
    (and (coll? a) (coll? b)) (into a b)
    :else b))


(defn get-sections 
  "Returns a vector of maps for each section"
  [yaml]
  (if-not (contains? yaml :sections)
    [yaml]
    (let [base-config (dissoc yaml :sections)]
      (mapv (fn [section]
              (deep-merge base-config section))
            (:sections yaml)))))


(defn- wrap-sections 
  "Wraps sections in a map with the key :sections"
  [yaml-vector]
  (when-not (vector? yaml-vector)
    (throw (IllegalArgumentException. "Input must be a vector")))
  {:sections (vec yaml-vector)})


(defn- write-file [path content]
  (spit path content))


(defn- get-yaml-string 
  "Returns a yaml formatted string representation of a map"
  [yaml]
  (yaml/generate-string yaml :dumper-options {:indent 2
                                              :flow-style :block}))


(defn write-solution [yaml path]
  (println "\nWriting solution to" path "\n")
  (->> yaml
       (wrap-sections)
       (get-yaml-string)
       (write-file path)))
