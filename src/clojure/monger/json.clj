;; This source code is dual-licensed under the Apache License, version
;; 2.0, and the Eclipse Public License, version 1.0.
;;
;; The APL v2.0:
;;
;; ----------------------------------------------------------------------------------
;; Copyright (c) 2011-2018 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;     http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.
;; ----------------------------------------------------------------------------------
;;
;; The EPL v1.0:
;;
;; ----------------------------------------------------------------------------------
;; Copyright (c) 2011-2018 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team.
;; All rights reserved.
;;
;; This program and the accompanying materials are made available under the terms of
;; the Eclipse Public License Version 1.0,
;; which accompanies this distribution and is available at
;; http://www.eclipse.org/legal/epl-v10.html.
;; ----------------------------------------------------------------------------------

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
                (-write
                  ([^ObjectId object out]
                   (clojure.data.json/write (.toString object) out))
                  ([^ObjectId object out options]
                   (clojure.data.json/write (.toString object) out options))))

              (extend-protocol clojure.data.json/JSONWriter
                BSONTimestamp
                (-write
                  ([^BSONTimestamp object out]
                   (clojure.data.json/write {:time (.getTime object) :inc (.getInc object)} out))
                  ([^BSONTimestamp object out options]
                   (clojure.data.json/write {:time (.getTime object) :inc (.getInc object)} out options))))

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
