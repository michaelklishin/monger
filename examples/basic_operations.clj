(ns examples.basic_operations
  (:gen-class)
  (:require [monger core collection util])
  (:import  (com.mongodb Mongo DB DBObject))
  (:use     [clojure.tools.cli]))

;; Make mongodb-settings accessible from the monger.core namespace
(monger.util/with-ns 'monger.core
  (def ^:dynamic *mongodb-settings*))

(defn fix-paul-maccartneys-name
  "Fix Paul McCartney's name"
  []
  (let [ paul_mccartney (monger.collection/find-one "people" { :first_name "Raul" }) ]


    ))

(do
  (let [
        args *command-line-args*
        parsed-args (cli args
                         (optional ["--port" "Mongodb port" :default 27017])
                         (optional ["--host" "Mongodb host" :default "localhost"])
                         (optional ["--db-name"             :default "monger-example"])) ]

    ;; Define Mongodb connection settings

    (def ^:dynamic *mongodb-settings* parsed-args)

    (monger.util/with-ns 'monger.core
      ;; Establish Mongodb connection
      (defonce ^:dynamic *mongodb-connection* (monger.core/connect *mongodb-settings*))
      ;; Connect to the database
      (defonce ^:dynamic *mongodb-database* (monger.core/get-db "monger-example")))

    (println "Does people connection exist: " (monger.collection/exists? "people"))

    ;; Insert a record to people collection
    (monger.collection/insert "people" { :first_name "John" :last_name "Lennon" })

    ;; Count an amount of records just inserted
    (println "People collection is: " (monger.collection/count "people"))

    ;; Insert several records
    (monger.collection/insert-batch "people" [{ :first_name "Ringo"  :last_name "Starr" }
                                              { :first_name "Raul"   :last_name "McCartney" }
                                              { :first_name "George" :last_name "Harrison" } ])

    (println "People collection is: " (monger.collection/count "people"))

    ;; Fix a typo in the inserted record
    (monger.collection/update "people" { :first_name "Raul" } { "$set" { :first_name "Paul" } })

    (println (monger.collection/find-one "people" { :first_name "Paul" }))

    ;; Now, let's add the index to that record
    (monger.collection/update "people" { :first_name "Paul" } { "$push" { :years_on_stage 1 } })

    ;; Increment record 45 times
    (dotimes [n 45]
            (monger.collection/update "people" { :first_name "Paul" } { "$inc" { :years_on_stage 1 } }))

    ;; Remove years_on_stage field
    (monger.collection/update "people" { :first_name "Paul" } { "$unset" { :years_on_stage 1} })

    ;; Insert the record to the data set if it wasn't there yet
    (monger.collection/update "people" { :first_name "Yoko" } { :first_name "Yoko" :last_name "Ono" } :upsert true)

    ;; Update multiple records
    (monger.collection/update "people" { } { "$set" { :band "The Beatles" }} :multi true)

    ;; Save can act both like insert and update
    (def ian_gillian
         (monger.convertion/to-db-object
          { :first_name "Ian" :last_name "Gillan" }))

    ;; Performs insert
    (monger.collection/save "people" ian_gillian)

    ;; Performs update
    (monger.collection/save "people"
       { :_id (monger.util/get-id ian_gillian)
         :first_name "Ian"
         :last_name "Gillan" :band "Deep Purple" })

    ;; Remove people collection
    (monger.collection/drop "people")
  ))
