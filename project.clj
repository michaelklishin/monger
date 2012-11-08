(defproject com.novemberain/monger "1.3.4"
  :description "Monger is a Clojure MongoDB client for a more civilized age: friendly, flexible and with batteries included"
  :url "http://clojuremongodb.info"
  :min-lein-version "2.0.0"
  :license {:name "Eclipse Public License"}
  :dependencies [[org.clojure/clojure           "1.4.0"]
                 [org.mongodb/mongo-java-driver "2.9.3"]
                 [com.novemberain/validateur    "1.2.0"]
                 [clojurewerkz/support          "0.9.0"]
                 [ragtime/ragtime.core          "0.3.0"]]
  :test-selectors {:default     (fn [m]
                                  (and (not (:performance m))
                                       (not (:edge-features m))
                                       (not (:time-consuming m))))
                   :focus         :focus
                   :updating      :updating
                   :indexing      :indexing
                   :external      :external
                   :cache         :cache
                   :gridfs        :gridfs
                   :command       :command
                   :integration   :integration
                   :performance   :performance
                   ;; as in, edge mongodb server
                   :edge-features  :edge-features
                   :time-consuming :time-consuming
                   :all           (constantly true)}
  :source-paths      ["src/clojure"]
  :java-source-paths ["src/java"]
  :javac-options     ["-target" "1.6" "-source" "1.6"]
  :mailing-list {:name "clojure-mongodb"
                 :archive "https://groups.google.com/group/clojure-mongodb"
                 :post "clojure-mongodb@googlegroups.com"}
  :profiles {:1.3   {:dependencies [[org.clojure/clojure "1.3.0"]]}
             :1.5   {:dependencies [[org.clojure/clojure "1.5.0-master-SNAPSHOT"]]}
             :dj01x {:dependencies [[org.clojure/data.json  "0.1.2" :exclusions [org.clojure/clojure]]]}
             :dj02x {:dependencies [[org.clojure/data.json  "0.2.1" :exclusions [org.clojure/clojure]]]}
             :dev {:resource-paths ["test/resources"]
                   :dependencies  [[clj-time "0.4.4"               :exclusions [org.clojure/clojure]]
                                   [cheshire               "4.0.2" :exclusions [org.clojure/clojure]]
                                   [org.clojure/tools.cli  "0.2.1" :exclusions [org.clojure/clojure]]
                                   [org.clojure/core.cache "0.6.1" :exclusions [org.clojure/clojure]]
                                   [ring/ring-core         "1.1.2"]]
                   :plugins [[codox "0.6.1"]]
                   :codox {:sources ["src/clojure"]
                           :output-dir "doc/api"
                           :exclude [monger.internal.pagination
                                     monger.internal.fn
                                     ;; these are not fully baked yet or have changes
                                     ;; that are not entirely backwards compatible with 1.0. MK.
                                     monger.testkit
                                     monger.ring.session-store]}}
             ;; only clj-time/JodaTime available, used to test monger.joda-time w/o clojure.data.json
             :dev2 {:resource-paths ["test/resources"]
                    :dependencies  [[clj-time "0.4.2"               :exclusions [org.clojure/clojure]]]}}
  :aliases {"all" ["with-profile" "dev:dev,1.3:dev,1.5:dev,dj01x:dev,dj02x"]}
  :repositories {"sonatype" {:url "http://oss.sonatype.org/content/repositories/releases"
                             :snapshots false
                             :releases {:checksum :fail :update :always}}
                 "sonatype-snapshots" {:url "http://oss.sonatype.org/content/repositories/snapshots"
                                       :snapshots true
                                       :releases {:checksum :fail :update :always}}}
  :aot [monger.conversion])
