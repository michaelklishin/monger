(ns monger.test.authentication
  (:require [monger core util db]
            [monger.test.helper :as helper])
  (:use [clojure.test]))

(helper/connect!)



(deftest test-authentication-with-valid-credentials
  ;; see ./bin/ci/before_script.sh. MK.
  (let [username "clojurewerkz/monger"
        pwd      "monger"]
    (is (monger.core/authenticate "monger-test" username (.toCharArray pwd)))))

(deftest test-authentication-with-invalid-credentials
  (let [username    "monger"
        ^String pwd (monger.util/random-str 128 32)]
    (is (not (monger.core/authenticate "monger-test2" username (.toCharArray pwd))))))
