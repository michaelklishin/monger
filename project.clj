(defproject com.novemberain/monger "1.0.0-SNAPSHOT"
  :description "Monger is an experimental idiomatic Clojure wrapper around MongoDB Java driver"
  :min-lein-version "2.0.0"
  :license {:name "Eclipse Public License"}
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.mongodb/mongo-java-driver "2.7.3"]
                 [com.novemberain/validateur "1.1.0-beta1"]]
  :test-selectors {:default (complement :performance)
                   :focus :focus
                   :indexing :indexing
                   :external :external
                   :performance :performance
                   :all (constantly true)}
  :codox {:exclude [monger.internal.pagination]}
  :mailing-list {:name "clojure-monger"
                 :archive "https://groups.google.com/group/clojure-monger"
                 :post "clojure-monger@googlegroups.com"}
  :profiles {:1.4 {:resource-paths ["test/resources"]
                   :dependencies [[org.clojure/clojure "1.4.0-beta7"]]}
             :dev {:resource-paths ["test/resources"]
                   :dependencies  [[clj-time "0.3.6"              :exclusions [org.clojure/clojure]]
                                   [codox "0.3.4"                 :exclusions [org.clojure/clojure]]
                                   [org.clojure/data.json "0.1.2" :exclusions [org.clojure/clojure]]
                                   [org.clojure/tools.cli "0.2.1" :exclusions [org.clojure/clojure]]
                                   [org.clojure/core.cache "0.5.0" :exclusions [org.clojure/clojure]]]}}
  :aliases { "all" ["with-profile" "dev:dev,1.4"] }
  :repositories {"sonatype" {:url "http://oss.sonatype.org/content/repositories/releases"
                             :snapshots false
                             :releases {:checksum :fail :update :always}}}
  :warn-on-reflection true)