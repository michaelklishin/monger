(defproject com.novemberain/monger "1.0.0-SNAPSHOT"
  :description "Monger is an experimental idiomatic Clojure wrapper around MongoDB Java driver"
  :license { :name "Eclipse Public License" }
  :mailing-list {:name "clojure-monger"
                 :archive "https://groups.google.com/group/clojure-monger"
                 :post "clojure-monger@googlegroups.com"}
  :repositories { "sonatype"
                  {:url "http://oss.sonatype.org/content/repositories/releases"
                   :snapshots false
                   :releases {:checksum :fail :update :always}
                   }}
  :dependencies [[org.clojure/clojure           "1.3.0"]
                 [org.mongodb/mongo-java-driver "2.7.3"]
                 [com.novemberain/validateur    "1.0.0"]]
  :multi-deps {
               "1.4" [[org.clojure/clojure "1.4.0-beta1"]]
               :all [[org.mongodb/mongo-java-driver "2.7.3"]
                     [com.novemberain/validateur    "1.0.0"]]
               }
  :dev-dependencies [[org.clojure/data.json "0.1.2" :exclusions [org.clojure/clojure]]
                     [clj-time              "0.3.6" :exclusions [org.clojure/clojure]]
                     [codox                 "0.3.4" :exclusions [org.clojure/clojure]]]
  :dev-resources-path "test/resources"
  :warn-on-reflection true
  :codox { :exclude [monger.internal.pagination] }
  :test-selectors   {:focus          (fn [v] (:focus v))})
