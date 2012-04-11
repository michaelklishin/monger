(ns monger.test.factory-dsl-test
  (:use     clojure.test
            [monger testkit joda-time]
            monger.test.fixtures
            [clj-time.core :only [days ago weeks now]])
  (:require [monger.collection  :as mc]
            [monger.test.helper :as helper])
  (:import org.bson.types.ObjectId
           org.joda.time.DateTime))


(helper/connect!)

(use-fixtures :each purge-domains purge-pages)



(defaults-for "domains"
  :ipv6-enabled false)

(let [coll "domains"]
  (factory coll "clojure"
           :name       "clojure.org"
           :created-at (-> 2 days ago)
           :embedded   [(embedded-doc "pages" "http://clojure.org/lisp")
                        (embedded-doc "pages" "http://clojure.org/jvm_hosted")
                        (embedded-doc "pages" "http://clojure.org/runtime_polymorphism")])

  (factory coll "elixir"
           :_id        (memoized-oid coll "elixir")
           :name       "elixir-lang.org"
           :created-at (fn [] (now))
           :topics     (fn [] ["programming" "erlang" "beam" "ruby"])
           :related    {
                        :terms (fn [] ["erlang" "python" "ruby"])
                        }))

(let [coll "pages"]
  (factory coll "http://clojure.org/rationale"
           :name "/rationale"
           :domain-id (parent-id "domains" "clojure"))
  (factory coll "http://clojure.org/jvm_hosted"
           :name "/jvm_hosted")
  (factory coll "http://clojure.org/runtime_polymorphism"
           :name "/runtime_polymorphism")
  (factory coll "http://clojure.org/lisp"
           :name "/lisp")
  (factory coll "http://elixir-lang.org/getting_started"
           :name "/getting_started/1.html"
           :domain-id (memoized-oid "domains" "elixir")))


(deftest test-building-documents-from-a-factory-case-1
  (let [t   (-> 2 weeks ago)
        doc (build "domains" "clojure" :created-at t)]
    (is (:_id doc))
    (is (= t (:created-at doc)))
    (is (= "clojure.org" (:name doc)))
    (is (false? (:ipv6-enabled doc)))))

(deftest test-building-documents-from-a-factory-case-2
  (let [oid (ObjectId.)
        doc (build "domains" "clojure" :_id oid)]
    (is (= oid (:_id doc)))
    (is (= "clojure.org" (:name doc)))
    (is (false? (:ipv6-enabled doc)))))

(deftest test-building-documents-from-a-factory-case-3
  (let [oid (ObjectId.)
        t   (-> 3 weeks ago)
        doc (build "domains" "clojure" :_id oid :created-at t :name "clojurewerkz.org" :ipv6-enabled true)]
    (is (= oid (:_id doc)))
    (is (= t (:created-at doc)))
    (is (= "clojurewerkz.org" (:name doc)))
    (is (:ipv6-enabled doc))
    (is (= ["/lisp" "/jvm_hosted" "/runtime_polymorphism"]
           (vec (map :name (:embedded doc)))))))


(deftest test-building-documents-from-a-factory-case-4
  (let [doc (build "domains" "elixir")]
    (is (:_id doc))
    (is (= (:_id doc) (memoized-oid "domains" "elixir")))
    (is (instance? DateTime (:created-at doc)))
    (is (= ["erlang" "python" "ruby"] (get-in doc [:related :terms])))
    (is (= "elixir-lang.org" (:name doc)))
    (is (not (:ipv6-enabled doc)))))

(deftest test-building-child-documents-with-a-parent-ref-case-1
  (let [doc (build "pages" "http://clojure.org/rationale")]
    (is (:domain-id doc))))

(deftest test-building-child-documents-that-use-memoized-oids-for-parents
  (let [doc (build "pages" "http://elixir-lang.org/getting_started")]
    (is (= (:domain-id doc) (memoized-oid "domains" "elixir")))))


(deftest test-seeding-documents-using-a-factory-case-1
  (is (mc/empty? "domains"))
  (let [t   (-> 2 weeks ago)
        doc (seed "domains" "clojure" :created-at t)]
    (is (= 1 (mc/count "domains")))
    (is (:_id doc))
    (is (= (:_id doc) (last-oid-of "domains" "clojure")))
    (is (= t (:created-at doc)))
    (is (= "clojure.org" (:name doc)))
    (is (false? (:ipv6-enabled doc)))))

(deftest test-seeding-documents-using-a-factory-case-2
  (is (mc/empty? "domains"))
  (let [doc    (seed "domains" "elixir")
        loaded (first (mc/find-maps "domains"))]
    (is (= 1 (mc/count "domains")))
    (is (:_id doc))
    (is (= (:_id doc) (:_id loaded)))
    (is (instance? DateTime (:created-at loaded)))
    (is (= ["erlang" "python" "ruby"] (get-in loaded [:related :terms])))
    (is (= "elixir-lang.org" (:name loaded)))
    (is (not (:ipv6-enabled loaded)))))



(deftest test-seeding-child-documents-with-a-parent-ref-case-1
  (is (mc/empty? "domains"))
  (is (mc/empty? "pages"))
  (let [page   (seed "pages" "http://clojure.org/rationale")
        domain (mc/find-map-by-id "domains" (:domain-id page))]
    (is (= 1 (mc/count "domains")))
    (is (= 1 (mc/count "pages")))
    (is domain)
    (is (:domain-id page))
    (is (= "clojure.org" (:name domain)))
    (is (= "/rationale" (:name page)))))


(deftest test-seeding-all-factories-in-a-group
  (is (mc/empty? "domains"))
  (is (mc/empty? "pages"))
  (seed-all "pages")
  (is (>= (mc/count "domains") 1))
  (is (>= (mc/count "pages") 4)))



(deftest test-named-memoized-object-ids
  (let [oid1 (memoized-oid "domains" "clojure.org")
        oid2 (memoized-oid "domains" "python.org")]
    (is (= oid1 (memoized-oid "domains" "clojure.org")))
    (is (= oid1 (memoized-oid "domains" "clojure.org")))
    (is (= oid1 (memoized-oid "domains" "clojure.org")))
    (is (= oid1 (memoized-oid "domains" "clojure.org")))
    (is (not (= oid1 oid2)))
    (is (= oid2 (memoized-oid "domains" "python.org")))
    (is (= oid2 (memoized-oid "domains" "python.org")))
    (is (= oid2 (memoized-oid "domains" "python.org")))))
