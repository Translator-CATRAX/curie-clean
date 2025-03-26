(ns curie-clean.processing.resolution-test
  (:require [clojure.test :refer [deftest testing is]]
            [curie-clean.processing.resolution :refer [resolve-duplicates process-files]]
            [curie-clean.processing.duplicates :as duplicates]))


(deftest process-files-test 
  (testing "Basic functionality with mock dependencies"
    (with-redefs [duplicates/get-duplicates (constantly {"dup1" #{"curie1" "curie2"}})
                  duplicates/no-duplicates? (constantly nil)
                  resolve-duplicates (constantly nil)]
      (let [input {:nodes "test.nodes" :edges "test.edges"}](is (nil? (process-files input)))))))
