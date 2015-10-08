(ns monger.test.authentication-test
  (:require [monger util db]
            [monger.credentials :as mcr]
            [monger.core :as mg]
            [monger.collection :as mc]
            [clojure.test :refer :all]))

;;
;; Connection via URI
;;

(when-not (System/getenv "CI")
  (deftest ^{:authentication true} connect-to-mongo-via-uri-without-credentials
    (let [{:keys [conn db]} (mg/connect-via-uri "mongodb://127.0.0.1/monger-test4")]
      (is (-> conn .getAddress (.sameHost "127.0.0.1")))))

  (deftest ^{:authentication true} connect-to-mongo-via-uri-with-valid-credentials
    (let [{:keys [conn db]} (mg/connect-via-uri "mongodb://clojurewerkz/monger:monger@127.0.0.1/monger-test4")]
      (is (= "monger-test4" (.getName db)))
      (is (-> conn .getAddress (.sameHost "127.0.0.1")))
      (mc/remove db "documents")
      ;; make sure that the database is selected
      ;; and operations get through.
      (mc/insert db "documents" {:field "value"})
      (is (= 1 (mc/count db "documents" {}))))))

(if-let [uri (System/getenv "MONGOHQ_URL")]
  (deftest ^{:external true :authentication true} connect-to-mongo-via-uri-with-valid-credentials
    (let [{:keys [conn db]} (mg/connect-via-uri uri)]
      (is (-> conn .getAddress (.sameHost "127.0.0.1"))))))


;;
;; Regular connecton
;;

(deftest ^{:authentication true} test-authentication-with-valid-credentials
    ;; see ./bin/ci/before_script.sh. MK.
  (doseq [s ["monger-test" "monger-test2" "monger-test3" "monger-test4"]]
    (let [creds (mcr/create "clojurewerkz/monger" "monger-test" "monger")
          conn  (mg/connect-with-credentials "127.0.0.1" creds)]
      (mc/remove (mg/get-db conn "monger-test") "documents"))))
