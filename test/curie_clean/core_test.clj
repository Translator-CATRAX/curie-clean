(ns curie-clean.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [curie-clean.core :refer [cli-options]]
            [clojure.tools.cli :refer [parse-opts]]))

(deftest test-arg-parser
  (testing "Are shorthand and long-form arguments parsed correctly"
    (is (= 
         {:nodes "nodes.txt" :edges "edges.txt"} 
         (get (parse-opts ["-n" "nodes.txt" "-e" "edges.txt"] cli-options) :options)))
    (is (=
         {:nodes "nodes.txt" :edges "edges.txt"}
         (get (parse-opts ["--nodes" "nodes.txt" "--edges" "edges.txt"] cli-options) :options)))))
