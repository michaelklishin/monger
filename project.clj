(defproject com.novemberain/monger "0.11.0-SNAPSHOT"
  :description "Monger is an experimental idiomatic Clojure wrapper around MongoDB Java driver"
  :license { :name "Eclipse Public License" }
  :dependencies [[org.clojure/clojure           "1.3.0"]
                 [org.mongodb/mongo-java-driver "2.6.5"]
                 [com.novemberain/validateur    "1.0.0-SNAPSHOT"]]
  :dev-dependencies [[org.clojure/data.json "0.1.1"]
                     [clj-time              "0.3.1"]]
  :dev-resources-path "test/resources"
  :warn-on-reflection true)
