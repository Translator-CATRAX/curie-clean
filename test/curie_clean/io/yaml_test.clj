(ns curie-clean.io.yaml-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clojure.java.io :as io]
            [curie-clean.io.yaml :refer [OrderedMapClass read-yaml force-regular-map get-sections]]
            [flatland.ordered.map :as omap]))

(def ^:dynamic *temp-yaml-path* nil)

(defn with-temp-yaml [f]
  (let [temp-file (java.io.File/createTempFile "test" ".yaml")]
    (try
      (binding [*temp-yaml-path* (.getAbsolutePath temp-file)]
        (f))
      (finally
        (when (.exists temp-file)
          (io/delete-file temp-file))))))


(use-fixtures :each with-temp-yaml)

(def test-yaml-content
  "
  common: value
  sections:
    - section1: foo
    - section2: bar
  ")

(def ordered-map-content
  (omap/ordered-map :a 1 :b (omap/ordered-map :c 3)))

(deftest read-yaml-test
  (testing "Can read basic YAML file"
    (spit *temp-yaml-path* test-yaml-content)
    (let [result (read-yaml *temp-yaml-path*)]
      (is (map? result))
      (is (= "value" (:common result))))))



(deftest force-regular-map-test
  (testing "Converts ordered maps to regular maps"
    (let [result (force-regular-map ordered-map-content)]
      (is (not (instance? OrderedMapClass result)))
      (is (= {:a 1 :b {:c 3}} result))))
  (testing "Preserves regular maps"
    (let [input {:normal :map}
          result (force-regular-map input)]
      (is (= input result)))))


(deftest get-sections-test 
  (testing "With sections" 
    (let [input {:common 1 :sections [{:a 1} {:b 2}]} 
          result (get-sections input)] 
      (is (= 2 (count result))) 
      (is (= {:common 1 :a 1} (first result)))) 
    (testing "Without sections" 
      (let [input {:standalone :config} 
            result (get-sections input)] 
        (is (= [input] result))))))


(deftest integration-test
  (testing "Full pipeline with ordered maps"
    (let [input (omap/ordered-map 
                 :common "val" 
                 :sections [(omap/ordered-map :section "A")])
          result (-> input 
                     force-regular-map 
                     get-sections 
                     first)] 
      (is (= {:common "val" :section "A"} result)))))
