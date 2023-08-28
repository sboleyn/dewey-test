(defproject os-test "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.cli "0.3.1"]
                 [cheshire "5.5.0"
                  :exclusions [[com.fasterxml.jackson.dataformat/jackson-dataformat-cbor]
                               [com.fasterxml.jackson.dataformat/jackson-dataformat-smile]
                               [com.fasterxml.jackson.core/jackson-annotations]
                               [com.fasterxml.jackson.core/jackson-databind]
                               [com.fasterxml.jackson.core/jackson-core]]]
                 [com.novemberain/langohr "3.5.1"]
                 [liberator "0.15.3"]
                 [compojure "1.1.8"]
                 [ring "1.4.0"]
                 [slingshot "0.10.3"]
                 [org.cyverse/clj-jargon "3.0.3"
                  :exclusions [[org.slf4j/slf4j-log4j12]
                               [log4j]]]
                 [org.cyverse/clojure-commons "2.8.0"]
                 [cc.qbits/spandex "0.7.11"]
                 [clj-commons/pomegranate "1.2.0"] ;;I added this; could remove?

                 [org.cyverse/service-logging "2.8.2"]]
  :eastwood {:exclude-namespaces [:test-paths]
             :linters [:wrong-arity :wrong-ns-form :wrong-pre-post :wrong-tag :misplaced-docstrings]}
  :plugins [[test2junit "1.1.3"]
            [jonase/eastwood "0.2.3"]]
  :resource-paths []
  :profiles {:dev     {:dependencies   [[midje "1.6.3"]]
                       :resource-paths ["dev-resources"]}
             :uberjar {:aot :all}}
  :jvm-opts ["-Dlogback.configurationFile=/etc/iplant/de/logging/dewey-logging.xml" "-javaagent:./opentelemetry-javaagent.jar" "-Dotel.resource.attributes=service.name=dewey"])
