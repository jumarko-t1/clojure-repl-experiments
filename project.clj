(defproject clojure-repl-experiments "0.1.0-SNAPSHOT"
  :description "My clojure REPL experiments."
  :url "https://github.com/jumarko/clojure-repl-experiments"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-beta2"]
                 [org.clojure/clojure "1.9.0-beta2" :classifier "sources"]
                 [org.apache.pdfbox/pdfbox "2.0.7"]
                 [net.java.dev.jna/jna "4.4.0"]
                 [org.flatland/useful "0.11.5"]
                 [org.apache.poi/poi-ooxml "3.17"]
                 [org.clojure/core.async "0.3.443"]
                 [funcool/cats "2.1.0"]
                 [buddy/buddy-hashers "1.3.0"]
                 [org.julienxx/clj-slack "0.5.5"]
                 [hswick/jutsu "0.1.1"]]
  :java-source-paths ["src/java"]
  :main ^:skip-aot clojure-repl-experiments.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
