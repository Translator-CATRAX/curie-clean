(ns curie-clean.io.tsv
    (:require [clojure.java.io :as io] 
              [clojure.data.csv :as csv])
    (:gen-class))

(import [org.apache.commons.csv CSVParser CSVFormat])


(defn read-tsv 
  "Reads a TSV file into a vector of vectors with Clojure CSV."
  [path]
  (with-open [reader (io/reader path)]
    (->> (line-seq reader)
         (pmap #(first (csv/read-csv (java.io.StringReader. %)
                                     :separator \tab)))
         doall)))


(defn add-index [data]
  (map-indexed (fn [idx row] (cons idx row)) data))


(defn separate-header [path]
  (let [[header & data] (add-index (read-tsv path))]
    {:header header :data data}))


(defn read-large-tsv 
  "Reads a large TSV file into a vector of maps with Commons CSV."
  [path]
  (with-open [reader (io/reader path)
              parser (CSVParser/parse reader
                                      (-> CSVFormat/TDF 
                                          (.withFirstRecordAsHeader)
                                          (.withIgnoreSurroundingSpaces)))]
    (let [data (vec (iterator-seq (.iterator parser)))]
      (pmap #(let [m (.toMap %)]
               (into {} (map (fn [[k v]] [(keyword k) (str v)]) m)))
            data))))
