;; Copyright (c) 2011-2014 Michael S. Klishin
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns monger.json
  "Provides clojure.data.json/Write-JSON protocol extension for MongoDB-specific types, such as
   org.bson.types.ObjectId"
  (:import org.bson.types.ObjectId
           org.bson.types.BSONTimestamp))

;;
;; Implementation
;;

;; copied from clojure.reducers
(defmacro ^:private compile-if
  "Evaluate `exp` and if it returns logical true and doesn't error, expand to
  `then`.  Else expand to `else`.

  (compile-if (Class/forName \"java.util.concurrent.ForkJoinTask\")
    (do-cool-stuff-with-fork-join)
    (fall-back-to-executor-services))"
  [exp then else]
  (if (try (eval exp)
           (catch Throwable _ false))
    `(do ~then)
    `(do ~else)))



;;
;; API
;;

(require 'clojurewerkz.support.json)

;; all this madness would not be necessary if some people cared about backwards
;; compatiblity of the libraries they maintain. Shame on the clojure.data.json maintainer. MK.
(compile-if (and (find-ns 'clojure.data.json)
                 clojure.data.json/JSONWriter)
            (try
              (extend-protocol clojure.data.json/JSONWriter
                ObjectId
                (-write [^ObjectId object out]
                  (clojure.data.json/write (.toString object) out)))

              (extend-protocol clojure.data.json/JSONWriter
                BSONTimestamp
                  (-write [^BSONTimestamp object out]
                    (clojure.data.json/write {:time (.getTime object) :inc (.getInc object)} out)))

              (catch Throwable _
                false))
            (comment "Nothing to do, clojure.data.json is not available"))

(compile-if (and (find-ns 'clojure.data.json)
                 clojure.data.json/Write-JSON)
            (try
              (extend-protocol clojure.data.json/Write-JSON
                ObjectId
                (write-json [^ObjectId object out escape-unicode?]
                  (clojure.data.json/write-json (.toString object) out escape-unicode?)))
              (catch Throwable _
                false))
            (comment "Nothing to do, clojure.data.json 0.1.x is not available"))


(try
  (require 'cheshire.generate)
  (catch Throwable t
    false))

(try
  (cheshire.generate/add-encoder ObjectId
                               (fn [^ObjectId oid ^com.fasterxml.jackson.core.json.WriterBasedJsonGenerator generator]
                                 (.writeString generator (.toString oid))))
    (cheshire.generate/add-encoder BSONTimestamp
                               (fn [^BSONTimestamp ts ^com.fasterxml.jackson.core.json.WriterBasedJsonGenerator generator]
                                 (cheshire.generate/encode-map {:time (.getTime ts) :inc (.getInc ts)} generator)))
  (catch Throwable t
    false))
