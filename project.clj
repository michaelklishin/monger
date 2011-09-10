(defproject com.novemberain/monger "0.5.0-SNAPSHOT"
  :description "Monger is an experimental idiomatic Clojure wrapper around MongoDB Java driver"
  :license { :name "Eclipse Public License" }
  :dependencies [[org.clojure/clojure "1.3.0-beta3"]
                 [org.mongodb/mongo-java-driver "2.6.5"]]
  :dev-dependencies [[org.clojure/tools.cli     "0.1.0"]]
  :warn-on-reflection true)