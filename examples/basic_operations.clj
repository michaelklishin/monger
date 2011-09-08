(ns examples.basic_operations
  (:gen-class)
  (:require [monger.core])
  (:import  (com.mongodb Mongo DB))
  (:use     [clojure.tools.cli]))


(do
  (let
      [ args *command-line-args*
       parsed-args (cli args
                        (optional ["--port" "Mongodb port" :default 27017])
                        (optional ["--host" "Mongodb host" :default "localhost"])
                        (optional ["--db-name"             :default "monger-example"])) ]
       ))

