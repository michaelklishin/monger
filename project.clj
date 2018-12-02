(defproject com.novemberain/monger "3.5.0-rc1"
  :description "Monger is a Clojure MongoDB client for a more civilized age: friendly, flexible and with batteries included"
  :url "http://clojuremongodb.info"
  :min-lein-version "2.5.1"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure        "1.9.0"]
                 [org.mongodb/mongodb-driver "3.9.1"]
                 [clojurewerkz/support       "1.1.0"]]
  :test-selectors {:default     (fn [m]
                                  (and (not (:performance m))
                                       (not (:edge-features m))
                                       (not (:time-consuming m))))
                   :focus          :focus
                   :authentication :authentication
                   :updating       :updating
                   :indexing       :indexing
                   :external       :external
                   :cache          :cache
                   :gridfs         :gridfs
                   :command        :command
                   :integration    :integration
                   :performance    :performance
                   ;; as in, edge mongodb server
                   :edge-features  :edge-features
                   :time-consuming :time-consuming
                   :all           (constantly true)}
  :source-paths      ["src/clojure"]
  :java-source-paths ["src/java"]
  :javac-options     ["-target" "1.7" "-source" "1.7"]
  :mailing-list {:name "clojure-mongodb"
                 :archive "https://groups.google.com/group/clojure-mongodb"
                 :post "clojure-mongodb@googlegroups.com"}
  :profiles {:1.8    {:dependencies [[org.clojure/clojure "1.8.0"]]}
             :master {:dependencies [[org.clojure/clojure "1.10.0-master-SNAPSHOT"]]}
             :dev {:resource-paths ["test/resources"]
                   :dependencies  [[clj-time               "0.15.1" :exclusions [org.clojure/clojure]]
                                   [cheshire               "5.8.1" :exclusions [org.clojure/clojure]]
                                   [org.clojure/data.json  "0.2.6" :exclusions [org.clojure/clojure]]
                                   [org.clojure/tools.cli  "0.4.1" :exclusions [org.clojure/clojure]]
                                   [org.clojure/core.cache "0.7.1" :exclusions [org.clojure/clojure]]
                                   [ring/ring-core         "1.7.1" :exclusions [org.clojure/clojure]]
                                   [com.novemberain/validateur "2.6.0" :exclusions [org.clojure/clojure]]
                                   [ch.qos.logback/logback-classic "1.2.3" :exclusions [org.slf4j/slf4j-api]]
                                   [ragtime/core "0.7.2" :exclusions [org.clojure/clojure]]]
                   :plugins [[lein-codox "0.10.5"]]
                   :codox {:source-paths ["src/clojure"]
                           :namespaces [#"^monger\.(?!internal)"]}}
             ;; only clj-time/JodaTime available, used to test monger.joda-time w/o clojure.data.json
             :dev2 {:resource-paths ["test/resources"]
                    :dependencies  [[clj-time "0.15.1"               :exclusions [org.clojure/clojure]]]}}
  :aliases {"all" ["with-profile" "dev:dev,1.8:dev,master"]}
  :repositories {"sonatype" {:url "https://oss.sonatype.org/content/repositories/releases"
                             :snapshots false
                             :releases {:checksum :fail :update :always}}
                 "sonatype-snapshots" {:url "https://oss.sonatype.org/content/repositories/snapshots"
                                       :snapshots true
                                       :releases {:checksum :fail :update :always}}})
