(defproject curie-clean "1.0.0"
  :description "A Clojure utility for analyzing and resolving duplicate nodes in Tablassert-integrated knowledge graphs."
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.cli "1.1.230"]
                 [org.clojure/data.csv "1.0.1"]
                 [org.apache.commons/commons-csv "1.10.0"]
                 [clj-commons/clj-yaml "1.0.29"]
                 [org.flatland/ordered "1.15.12"]]
  :main ^:skip-aot curie-clean.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
