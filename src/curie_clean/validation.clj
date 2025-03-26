(ns curie-clean.validation
  (:require [clojure.java.io :as io])
  (:gen-class))


(defn- is-file? [path]
  (.exists (io/file path)))


(defn- is-tsv? [path]
  (.endsWith path ".tsv"))


(defn- file-readable? [path]
  (let [file (io/file path)]
    (.canRead file)))


(defn- empty-tsv? [path]
  (empty? (line-seq (io/reader path))))


(defn is-valid-tsv? [path file-type]
  (cond
    (not (is-file? path)) (format "%s file does not exist" file-type)
    (not (is-tsv? path)) (format "%s file is not a TSV" file-type)
    (not (file-readable? path)) (format "%s file is not readable" file-type)
    (empty-tsv? path) (format "%s file is empty" file-type)
    :else true))


(defn tsv-validator [file-type]
  (fn [path]
    (let [result (is-valid-tsv? path file-type)]
      (if (string? result) result true))))
