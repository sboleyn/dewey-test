(defproject os-test "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.cli "0.3.1"]
                 [slingshot "0.10.3"]
                 [org.cyverse/clj-jargon "3.0.3"
                  :exclusions [[org.slf4j/slf4j-log4j12]
                               [log4j]]]
                 [org.cyverse/clojure-commons "2.8.0"]
                 [cc.qbits/spandex "0.7.11"]
                 [clj-commons/pomegranate "1.2.0"] ;;I added this; could remove?
                 ]
  :repl-options {:init-ns os-test.core})
