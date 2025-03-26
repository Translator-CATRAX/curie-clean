(ns curie-clean.processing.duplicates-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [curie-clean.processing.duplicates :as dup]
            [clojure.java.io :as io]
            [clojure.string :as str]))


(def mock-tsv-data "id\tname\tcategory\n1\tfoo\tbar\n2\tfoo\tbaz\n3\tunique\tqux")
(def mock-tsv-path (io/file "test_mock.tsv"))

(def mock-metadata
  [{:subject "CURIE:1" :object "CURIE:2" :config_path "file1" :section "section1"}
   {:subject "CURIE:2" :object "CURIE:3" :config_path "file2" :section "section2"}])

(defn tsv-fixture [f]
  (spit mock-tsv-path mock-tsv-data)
  (f)
  (io/delete-file mock-tsv-path))


(use-fixtures :once tsv-fixture)

(deftest get-duplicates-test
  (testing "finds duplicates by name column"
    (let [result (dup/get-duplicates mock-tsv-path)]
      (is (= {"foo" ['(1 "1" "foo" "bar") '(2 "2" "foo" "baz")]} result)))) 
  (testing "throws when name column not found"
    (let [bad-data "id\tnotname\tcategory\n1\tfoo\tbar"
          bad-path (io/file "bad_mock.tsv")]
      (spit bad-path bad-data)
      (is (thrown? Exception (dup/get-duplicates bad-path)))
      (io/delete-file "bad_mock.tsv"))))


(deftest print-dups-test
  (testing "prints duplicate information"
    (let [output (with-out-str
                   (dup/print-dups "test"
                                   [["1" "CURIE:1"] ["2" "CURIE:2"]]
                                   {"CURIE:1" {"file1" {}} "CURIE:2" {"file2" {}}}))]
      (is (str/includes? output "\"test\" has 2 duplicate CURIES:"))
      (is (str/includes? output "1: CURIE:1  file1"))
      (is (str/includes? output "2: CURIE:2  file2")))))


(deftest parse-input-test
  (testing "parses valid input"
    (is (= #{1 3} (dup/parse-input "1, 3" 5))) 
    (is (= #{2} (dup/parse-input "2" 5))) 
    (is (= #{0} (dup/parse-input "0" 5))))
  (testing "filters invalid input" 
    (is (= #{} (dup/parse-input "6" 5))) 
    (is (= #{} (dup/parse-input "abc" 5))) 
    (is (= #{1} (dup/parse-input "1,abc" 5)))))


(deftest prompt-user-test
  (testing "returns selected curies"
    (with-in-str "1,3\n"
      (with-redefs [read-line (fn [] "1,3")]
        (is (= #{["1" "CURIE:1"] ["3" "CURIE:3"]}
               (dup/prompt-user [["1" "CURIE:1"] ["2" "CURIE:2"] ["3" "CURIE:3"]])))))))
  

(deftest get-metadata-test
  (testing "finds metadata for selected curies"
    (let [result (dup/get-metadata mock-metadata [["1" "CURIE:2"]])]
      (is (= {"CURIE:2" {"file1" {:object true :section "section1"}
                         "file2" {:subject true :section "section2"}}} 
             result)))))
