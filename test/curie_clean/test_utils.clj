(ns curie-clean.test-utils
  (:require [clojure.java.io :as io]))
  
(def ^:dynamic *test-tsv-path* nil)
  
(def ^:dynamic *test-tsv-content* 
  "id\tname\tcategory\n1\tAPOA1\tbiolink:Gene\n2\tBRCA1\tbiolink:Gene\n")

(defn with-temp-tsv 
  "Creates a temporary TSV file and binds *test-tsv-path* to its path."
  [f] 
  (let [temp-file (java.io.File/createTempFile "test" ".tsv")] 
    (try 
      (with-open [writer (io/writer temp-file)] 
        (.write writer *test-tsv-content*)) 
      (binding [*test-tsv-path* (.getAbsolutePath temp-file)] 
        (f)) 
      (finally 
        (when (.exists temp-file) 
          (io/delete-file temp-file))))))
  