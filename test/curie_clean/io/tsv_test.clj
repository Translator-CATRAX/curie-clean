(ns curie-clean.io.tsv-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [curie-clean.io.tsv :refer [read-large-tsv read-tsv separate-header]]
            [curie-clean.test-utils :refer [*test-tsv-path* with-temp-tsv]]))


(use-fixtures :each with-temp-tsv)

(deftest test-read-large-tsv 
  (testing "Can parse TSV with headers" 
    (let [result (read-large-tsv *test-tsv-path*)]
      (is (= 2 (count result)))
      (is (= {:id "1" :name "APOA1" :category "biolink:Gene"}
             (first result))))))


(deftest test-read-tsv 
  (testing "Basic TSV reading" 
    (is (= [["id" "name" "category"] 
            ["1" "APOA1" "biolink:Gene"] 
            ["2" "BRCA1" "biolink:Gene"]] 
           (read-tsv *test-tsv-path*)))))

  
(deftest test-separate-header 
  (testing "Header separation" 
    (let [{:keys [header data]} (separate-header *test-tsv-path*)] 
      (is (= [0 "id" "name" "category"] header)) 
      (is (= 2 (count data))))))
