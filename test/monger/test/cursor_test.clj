(set! *warn-on-reflection* true)

(ns monger.test.cursor-test
  (:import  [com.mongodb DBCursor DBObject Bytes]
            [java.util List Map])
  (:require [monger.test.helper :as helper])
  (:use clojure.test
        monger.cursor
        monger.test.fixtures))

(helper/connect!)

(deftest make-db-cursor-for-collection
  (is (= DBCursor
          (class (make-db-cursor :docs)))))

(deftest getting-cursor-options-value
  (let [db-cur (make-db-cursor :docs)
        opts (get-options db-cur)]
    (is (= true (isa? (class opts) Map)))
    (is (= 0 (.getOptions db-cur))) ;;test default value
    (is (= false (:notimeout opts)))
    (is (= false (:partial opts)))
    (is (= false (:awaitdata opts)))
    (is (= false (:oplogreplay opts)))
    (is (= false (:slaveok opts)))
    (is (= false (:tailable opts)))))

(deftest adding-option-to-cursor
  (let [db-cur (make-db-cursor :docs)
        _ (add-option! db-cur :notimeout)]
    (is (= (:notimeout cursor-options)
           (.getOptions db-cur)))
    (add-option! db-cur :tailable)
    (is (= (.getOptions db-cur)
           (bit-or (:notimeout cursor-options)
                   (:tailable cursor-options))))))

(deftest remove-option-from-cursor
  (let [db-cur (make-db-cursor :docs)]
    (add-option! db-cur :partial)
    (add-option! db-cur :awaitdata)
    ;; removing not-set option should not affect result
    (remove-option! db-cur :notimeout)
    (is (= (.getOptions db-cur)
           (bit-or (:partial cursor-options)
                   (:awaitdata cursor-options))))
    ;; removing active option should remove correct value
    (remove-option! db-cur :awaitdata)
    (is (= (.getOptions db-cur)
           (:partial cursor-options)))))


(deftest test-reset-options
  (let [db-cur (make-db-cursor :docs)]
    (add-option! db-cur :partial)
    (is (= (.getOptions db-cur)
           (:partial cursor-options)))
    (is (= 0
           (int (.getOptions (reset-options db-cur)))))))

(deftest add-options-with-hashmap
  (let [db-cur (make-db-cursor :docs)
          _ (add-options db-cur {:notimeout true :slaveok true})
        opts (get-options db-cur)]
    (is (= true (:notimeout opts)))
    (is (= true (:slaveok opts)))
    (is (= false (:tailable opts)))
    (is (= false (:oplogreplay opts)))))

(deftest add-options-with-hashmap-and-remove-option
  (let [db-cur (make-db-cursor :docs)
          _ (add-options db-cur {:notimeout true :slaveok true})
        opts (get-options db-cur)]
    (is (= true (:notimeout opts)))
    (is (= true (:slaveok opts)))
    ;;remove key and add another option
    (add-options db-cur {:partial true :slaveok false})
    (let [opts (get-options db-cur)]
      (is (= true (:notimeout opts)))
      (is (= true (:partial opts)))
      (is (= false (:slaveok opts)))
      (is (= false (:tailable opts))))))

(deftest add-options-with-list
  (let [db-cur (make-db-cursor :docs)
        _ (add-options db-cur [:notimeout :slaveok])
        opts (get-options db-cur)]
    (is (= true (:notimeout opts)))
    (is (= true (:slaveok opts)))
    (is (= false (:tailable opts)))
    (is (= false (:oplogreplay opts)))))

(deftest add-options-with-Bytes
  (let [db-cur (make-db-cursor :docs)
        _ (add-options db-cur Bytes/QUERYOPTION_NOTIMEOUT)
        opts (get-options db-cur)]
    (is (= true (:notimeout opts)))
    (is (= false (:slaveok opts)))
    (is (= false (:tailable opts)))
    (is (= false (:oplogreplay opts)))))

(deftest add-options-with-one-keyword
  (let [db-cur (make-db-cursor :docs)
        _ (add-options db-cur :notimeout)
        opts (get-options db-cur)]
    (is (= true (:notimeout opts)))
    (is (= false (:slaveok opts)))
    (is (= false (:tailable opts)))
    (is (= false (:oplogreplay opts)))))
