(ns monger.test.authentication-test
  (:require [monger util db]
            [monger.core :as mg]
            [monger.collection :as mc]
            [clojure.test :refer :all]))

;;
;; Connection via URI
;;

(when-not (System/getenv "CI")
  (deftest ^{:authentication true} connect-to-mongo-via-uri-without-credentials
    (let [{:keys [conn db]} (mg/connect-via-uri "mongodb://127.0.0.1/monger-test4")]
      (is (= (-> conn .getAddress ^InetAddress (.sameHost "127.0.0.1"))))))

  (deftest ^{:authentication true} connect-to-mongo-via-uri-with-valid-credentials
    (let [{:keys [conn db]} (mg/connect-via-uri "mongodb://clojurewerkz/monger:monger@127.0.0.1/monger-test4")]
      (is (= "monger-test4" (.getName db)))
      (is (= (-> conn .getAddress ^InetAddress (.sameHost "127.0.0.1"))))
      (mc/remove db "documents")
      ;; make sure that the database is selected
      ;; and operations get through.
      (mc/insert db "documents" {:field "value"})
      (is (= 1 (mc/count db "documents" {}))))))

(if-let [uri (System/getenv "MONGOHQ_URL")]
  (deftest ^{:external true :authentication true} connect-to-mongo-via-uri-with-valid-credentials
    (let [{:keys [conn db]} (mg/connect-via-uri uri)]
      (is (= (-> conn .getAddress ^InetAddress (.sameHost "127.0.0.1")))))))


;;
;; Regular connecton
;;

(let [conn (mg/connect)
      db   (mg/get-db conn "monger-test")]
  (deftest ^{:authentication true} test-authentication-with-valid-credentials-on-the-default-db
    ;; see ./bin/ci/before_script.sh. MK.
    (let [username "clojurewerkz/monger"
          pwd      "monger"]
      (is (mg/authenticate db username (.toCharArray pwd)))))

  (deftest ^{:authentication true} test-authentication-with-valid-credentials-on-an-arbitrary-db
    ;; see ./bin/ci/before_script.sh. MK.
    (let [username "clojurewerkz/monger"
          pwd      "monger"]
      (is (mg/authenticate (mg/get-db conn "monger-test") username (.toCharArray pwd)))))

  (deftest ^{:authentication true} test-authentication-with-invalid-credentials
    (let [username    "monger"
          ^String pwd (monger.util/random-str 128 32)]
      (is (not (mg/authenticate (mg/get-db conn "monger-test2") username (.toCharArray pwd)))))))
