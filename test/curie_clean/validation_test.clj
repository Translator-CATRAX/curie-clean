(ns curie-clean.validation-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [curie-clean.validation :refer [tsv-validator]]
            [curie-clean.test-utils :refer [*test-tsv-path* with-temp-tsv]]))


(use-fixtures :each with-temp-tsv)

(deftest tsv-validation-test
  (let [validator-fn (tsv-validator "Test TSV")]
    (testing "Valid TSV file"
      (is (true? (validator-fn *test-tsv-path*))))
    (testing "Nonexistent file"
      (is (string? (validator-fn "/nonexistent/path/file.tsv"))))))
