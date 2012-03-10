(defproject com.novemberain/monger "1.0.0-SNAPSHOT"
  :description "Monger is an experimental idiomatic Clojure wrapper around MongoDB Java driver"
  :min-lein-version "2.0.0"
  :license {:name "Eclipse Public License"}
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.mongodb/mongo-java-driver "2.7.3"]
                 [com.novemberain/validateur "1.0.0"]]
  :test-selectors {:focus (fn [v] (:focus v))}
  :codox {:exclude [monger.internal.pagination]}
  :mailing-list {:name "clojure-monger",
                 :archive "https://groups.google.com/group/clojure-monger",
                 :post "clojure-monger@googlegroups.com"}
  :profiles {:all {:dependencies [[org.mongodb/mongo-java-driver "2.7.3"]
                                  [com.novemberain/validateur "1.0.0"]]},
             :1.4 {:resource-paths ["test/resources"],
                   :dependencies [[org.clojure/clojure "1.4.0-beta4"]
                                  [org.clojure/data.json "0.1.2" :exclusions [org.clojure/clojure]]
                                  [clj-time "0.3.6"              :exclusions [org.clojure/clojure]]
                                  [codox "0.3.4"                 :exclusions [org.clojure/clojure]]
                                  [org.clojure/tools.cli "0.2.1" :exclusions [org.clojure/clojure]]]},
             :dev {:resource-paths ["test/resources"],
                   :dependencies  [[org.clojure/data.json "0.1.2" :exclusions [org.clojure/clojure]]
                                   [clj-time "0.3.6"              :exclusions [org.clojure/clojure]]
                                   [codox "0.3.4"                 :exclusions [org.clojure/clojure]]
                                   [org.clojure/tools.cli "0.2.1" :exclusions [org.clojure/clojure]]]}}
  :repositories {"sonatype" {:url "http://oss.sonatype.org/content/repositories/releases",
                             :snapshots false,
                             :releases {:checksum :fail, :update :always}}}
  :warn-on-reflection true)