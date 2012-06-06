(defproject com.novemberain/monger "1.0.0-SNAPSHOT"
  :description "Monger is a Clojure MongoDB client for a more civilized age: friendly, flexible and with batteries included"
  :min-lein-version "2.0.0"
  :license {:name "Eclipse Public License"}
  :dependencies [[org.clojure/clojure           "1.3.0"]
                 [org.mongodb/mongo-java-driver "2.7.3"]
                 [com.novemberain/validateur    "1.1.0"]
                 [clojurewerkz/support          "0.4.0"]]
  :test-selectors {:default     (fn [m]
                                  (and (not (:performance m))
                                       (not (:edge-features m))))
                   :focus         :focus
                   :indexing      :indexing
                   :external      :external
                   :cache         :cache
                   :gridfs        :gridfs                   
                   :performance   :performance
                   ;; as in, edge mongodb server
                   :edge-features :edge-features
                   :all           (constantly true)}
  :codox {:exclude [monger.internal.pagination]}
  :mailing-list {:name "clojure-monger"
                 :archive "https://groups.google.com/group/clojure-mongodb"
                 :post "clojure-mongodb@googlegroups.com"}
  :profiles {:1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.0-master-SNAPSHOT"]]}
             :dev {:resource-paths ["test/resources"]
                   :dependencies  [[clj-time "0.4.2"              :exclusions [org.clojure/clojure]]
                                   [org.clojure/data.json  "0.1.2" :exclusions [org.clojure/clojure]]
                                   [org.clojure/tools.cli  "0.2.1" :exclusions [org.clojure/clojure]]
                                   [org.clojure/core.cache "0.5.0" :exclusions [org.clojure/clojure]]
                                   [ring/ring-core         "1.1.0"]]}}
  :aliases {"all" ["with-profile" "dev:dev,1.4:dev,1.5"]
            "ci"  ["with-profile" "dev:dev,1.4"]}
  :repositories {"sonatype" {:url "http://oss.sonatype.org/content/repositories/releases"
                             :snapshots false
                             :releases {:checksum :fail :update :always}}
                 "sonatype-snapshots" {:url "http://oss.sonatype.org/content/repositories/snapshots"
                                       :snapshots true
                                       :releases {:checksum :fail :update :always}}}
  :warn-on-reflection true)
