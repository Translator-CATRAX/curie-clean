(ns curie-clean.processing.duplicates
    (:require [curie-clean.io.tsv :as tsv]
              [clojure.string :as str])
    (:gen-class))


(defn get-duplicates 
  "Returns a map of duplicates indexed by name"
  [path]
  (let [{:keys [header data]} (tsv/separate-header path)
        col-names (rest header)
        name-pos (->> col-names
                      (map-indexed vector)
                      (filter #(= "name" (second %)))
                      (ffirst))]
    (when (nil? name-pos)
      (throw (Exception. "\"name\" column not found")))
    (->> data
         (group-by #(nth % (inc name-pos)))
         (filter #(> (count (second %)) 1))
         (into {}))))


(defn no-duplicates? [dups]
  (when (empty? dups)
    (println "\nNo duplicate nodes found")
    (System/exit 0)))


(defn print-dups [dup curies  metadata-map]
  (let [dup-count (count curies)]
    (println (str "\n\"" dup "\" has " dup-count " duplicate CURIES:"))
    (doseq [[index [_ curie _ category]] (map-indexed vector curies)]
      (let [file (->> (get metadata-map curie)
                      keys)]
        (println (str (inc index) ": " curie " " category " " (first file)))))))


(defn parse-input 
  "Converts a comma separated list of numbers into a set of numbers"
  [input max-number]
  (->> (clojure.string/split input #",")
       (map clojure.string/trim)
       (map #(try (Integer/parseInt %)
                  (catch NumberFormatException _ -1)))
       (filter #(<= 0 % max-number))
       set))


(defn prompt-user [curies]
  (print (str "Enter numbers (1-" (count curies) ") of invalid duplicates (comma separated), "
              "or 0 to accept all: "))
  (flush)
  (let [input (read-line)
        max-idx (count curies)
        indices (parse-input input max-idx)]
    (if (contains? indices 0)
      #{}
      (->> indices
           (map #(nth curies (dec %)))
           set))))


(defn- in-subject-object? 
  "Returns a map of columns to true if the curie is in the subject or object"
  [record curie]
  (cond-> {}
    (= curie (:subject record)) (assoc :subject true)
    (= curie (:object record))  (assoc :object true)))


(defn- process-match [data [_ curie]]
  (let [matches (filter #(or (= curie (str (:subject %)))
                             (= curie (str (:object %))))
                        data)]
    (when (seq matches)
      [curie (reduce (fn [acc record]
                       (let [path (:config_path record)
                             section (:section record)
                             cols (in-subject-object? record curie)
                             cols-with-section (assoc cols :section section)]
                         (update acc path #(merge-with (fn [a b] (or a b)) % cols-with-section))))
                     {}
                     matches)])))


(defn get-metadata [data selected-curies]
  (into {} (keep #(process-match data %) selected-curies)))
