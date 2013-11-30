(ns monger.test.authentication-test
  (:require [monger core util db]
            [monger.test.helper :as helper]
            [monger.collection :as mc]
            [clojure.test :refer :all]))

(helper/connect!)


(when-not (System/getenv "CI")
  (deftest ^{:authentication true} connect-to-mongo-via-uri-without-credentials
    (let [connection (monger.core/connect-via-uri! "mongodb://127.0.0.1/monger-test4")]
      (is (= (-> connection .getAddress ^InetAddress (.sameHost "127.0.0.1")))))
    ;; reconnect using regular host
    (helper/connect!))

  (deftest ^{:authentication true} connect-to-mongo-via-uri-with-valid-credentials
    (let [connection (monger.core/connect-via-uri! "mongodb://clojurewerkz/monger:monger@127.0.0.1/monger-test4")]
      (is (= "monger-test4" (.getName (monger.core/current-db))))
      (is (= (-> connection .getAddress ^InetAddress (.sameHost "127.0.0.1"))))
      (mc/remove "documents")
      ;; make sure that the database is selected
      ;; and operations get through.
      (mc/insert "documents" {:field "value"})
      (is (= 1 (mc/count "documents" {}))))
    ;; reconnect using regular host
    (helper/connect!)))

(if-let [uri (System/getenv "MONGOHQ_URL")]
  (deftest ^{:external true :authentication true} connect-to-mongo-via-uri-with-valid-credentials
    (let [connection (monger.core/connect-via-uri! uri)]
      (is (= (-> connection .getAddress ^InetAddress (.sameHost "127.0.0.1")))))
    ;; reconnect using regular host
    (helper/connect!)))


(deftest ^{:authentication true} test-authentication-with-valid-credentials-on-the-default-db
  ;; see ./bin/ci/before_script.sh. MK.
  (let [username "clojurewerkz/monger"
        pwd      "monger"]
    (is (monger.core/authenticate username (.toCharArray pwd)))))

(deftest ^{:authentication true} test-authentication-with-valid-credentials-on-an-arbitrary-db
  ;; see ./bin/ci/before_script.sh. MK.
  (let [username "clojurewerkz/monger"
        pwd      "monger"]
    (is (monger.core/authenticate (monger.core/get-db "monger-test") username (.toCharArray pwd)))))

(deftest ^{:authentication true} test-authentication-with-invalid-credentials
  (let [username    "monger"
        ^String pwd (monger.util/random-str 128 32)]
    (is (not (monger.core/authenticate (monger.core/get-db "monger-test2") username (.toCharArray pwd))))))
