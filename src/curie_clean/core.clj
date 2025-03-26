(ns curie-clean.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as str]
            [curie-clean.validation :as validation]
            [curie-clean.processing.resolution :as resolution])
  (:gen-class))


(def cli-options
  [["-n" "--nodes FILE" "Please provide a Nodes File"
    :required true
    :validate [(validation/tsv-validator "Nodes")]
    :missing "Please specify a Nodes file using -n or --nodes"]
   ["-e" "--edges FILE" "Please provide an Edges File"
    :required true
    :validate [(validation/tsv-validator "Edges")]
    :missing "Please specify an Edges file using -e or --edges"]
   ["-h" "--help"]])


(defn -main [& args]
  (let [{:keys [options errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) (println summary)
      errors (do (println "Errors:\n" (str/join \newline errors))
                 (System/exit 1))
      :else (resolution/process-files options))))
