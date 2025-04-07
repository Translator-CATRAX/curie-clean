(ns curie-clean.processing.resolution
    (:require [curie-clean.io.yaml :as yaml] 
              [curie-clean.processing.duplicates :as duplicates]
              [curie-clean.io.tsv :as tsv]
              [clojure.string :as str])
    (:gen-class))


(defn- print-solutions [problem file]
  (println (str "\nHow do you want to resolve \"" problem "\" in " file "?"))
  (println "0: add expected_classes")
  (println "1: add classes_to_avoid")
  (println "2: add expected_taxa")
  (println "3: add regex_replacements"))


(defn- get-solution []
  (print "Enter a number (0-3): ")
  (flush)
  (let [input (read-line)]
    (duplicates/parse-input input 3)))


(defn- parse-vector-input [input]
  (->> (clojure.string/split input #",")
       (map clojure.string/trim)
       (remove empty?)
       vec))


(defn- get-classes-solution [mode]
  (println (str "\nEnter " mode "(comma separated): "))
  (flush)
  (let [input (read-line)]
    {(keyword mode) (parse-vector-input input)}))


(defn- append-to-nth-map 
  "Updates the nth map in a vector of maps"
  [yaml-map section columns fix]
  (reduce (fn [updated-yaml column]
            (update-in updated-yaml [section column]
                       (fn [existing-map]
                         (reduce-kv (fn [m k new-items]
                                      (update m k (fn [existing-vector]
                                                    (if existing-vector
                                                      (vec (concat existing-vector new-items))
                                                      (vec new-items)))))
                                    (or existing-map {})
                                    fix))))
          yaml-map
          columns))


(defn- apply-expected-classes [yaml-map path {:keys [subject object section]}]
  (let [expected-classes (get-classes-solution "expected_classes")
        num (-> section str Integer/parseInt dec)]
    (cond
      (and subject object) (yaml/write-solution (append-to-nth-map yaml-map num [:subject :object] expected-classes) path)
      (and subject (not object)) (yaml/write-solution (append-to-nth-map yaml-map num [:subject] expected-classes) path)
      (and (not subject) object) (yaml/write-solution (append-to-nth-map yaml-map num [:object] expected-classes) path)
      :else (throw (Exception. "Curie must be in subject or object")))))


(defn- apply-classes-to-avoid [yaml-map path {:keys [subject object section]}]
  (let [classes-to-avoid (get-classes-solution "classes_to_avoid")
        num (-> section str Integer/parseInt dec)]
    (cond
      (and subject object) (yaml/write-solution (append-to-nth-map yaml-map num [:subject :object] classes-to-avoid) path)
      (and subject (not object)) (yaml/write-solution (append-to-nth-map yaml-map num [:subject] classes-to-avoid) path)
      (and (not subject) object) (yaml/write-solution (append-to-nth-map yaml-map num [:object] classes-to-avoid) path)
      :else (throw (Exception. "Curie must be in subject or object")))))


(defn- apply-expected-taxa [yaml-map path {:keys [subject object section]}]
  (let [expected-taxa (get-classes-solution "expected_taxa")
        num (-> section str Integer/parseInt dec)]
    (cond
      (and subject object) (yaml/write-solution (append-to-nth-map yaml-map num [:subject :object] expected-taxa) path)
      (and subject (not object)) (yaml/write-solution (append-to-nth-map yaml-map num [:subject] expected-taxa) path)
      (and (not subject) object) (yaml/write-solution (append-to-nth-map yaml-map num [:object] expected-taxa) path)
      :else (throw (Exception. "Curie must be in subject or object")))))


(defn- get-regex-solution []
  (println "\nEnter pattern and replacement (comma separated): ")
  (flush)
  (let [[pattern replacement & _] (parse-vector-input (read-line))] 
    {:regex_replacements [{:pattern (or pattern "") 
                           :replacement (or replacement "")}]}))


(defn- apply-regex-replacements [yaml-map path {:keys [subject object section]}]
  (let [regex-replacements (get-regex-solution)
        num (-> section str Integer/parseInt dec)]
    (cond
      (and subject object) (yaml/write-solution (append-to-nth-map yaml-map num [:subject :object] regex-replacements) path)
      (and subject (not object)) (yaml/write-solution (append-to-nth-map yaml-map num [:subject] regex-replacements) path)
      (and (not subject) object) (yaml/write-solution (append-to-nth-map yaml-map num [:object] regex-replacements) path)
      :else (throw (Exception. "Curie must be in subject or object")))))


(defn- apply-solution [yaml attrs solution]
  (let [yaml-map (yaml/get-sections (yaml/force-regular-map  (yaml/read-yaml yaml)))]
    (case (first solution)
      0 (apply-expected-classes yaml-map yaml attrs)
      1 (apply-classes-to-avoid yaml-map yaml attrs)
      2 (apply-expected-taxa yaml-map yaml attrs)
      3 (apply-regex-replacements yaml-map yaml attrs)
      :else (throw (Exception. "Invalid solution")))))


(defn resolve-duplicates [dups edges]
  (let [data (tsv/read-large-tsv edges)]
    (into {}
          (for [[dup curies] dups] 
            (let [metadata-map (duplicates/get-metadata data curies)]
              (duplicates/print-dups dup curies metadata-map)
              (let [selected (duplicates/prompt-user curies)
                    filtered-metadata (select-keys metadata-map (vec (map second selected)))]
                (doseq [[problem metadata] filtered-metadata]
                  (doseq [[yaml attrs] metadata]
                    (print-solutions problem yaml)
                    (let [solution (get-solution)]
                      (apply-solution yaml attrs solution)))))))))
  (println "\nCongrats! You've dealt with all the duplicates!!\n")
  (System/exit 1))


(defn- get-problematic-curies [nodes edges]
  (let [node-dups (duplicates/get-duplicates nodes)]
    (duplicates/no-duplicates? node-dups)
    (println "\nDuplicate nodes:" (count node-dups))
    (resolve-duplicates node-dups edges)))


(defn process-files [{:keys [nodes edges]}]
  (get-problematic-curies nodes edges))
