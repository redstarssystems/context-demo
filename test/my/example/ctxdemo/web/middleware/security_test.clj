(ns my.example.ctxdemo.web.middleware.security-test
  (:require [clojure.test :refer :all]
            [matcho.core :refer [match]]
            [unifier.response :as r]
            [my.example.ctxdemo.web.middleware.security :as sut]))

(deftest admin-network?-test
  (match (#'sut/admin-network? ["127.0.0.0/8"] "127.0.0.3") true)
  (match (#'sut/admin-network? ["127.0.0.0/8"] "128.0.0.1") false)
  (match (#'sut/admin-network? [""] "127.0.0.1") false)
  (match (#'sut/admin-network? ["0.0.0.0/0"] "192.16.3.4") true) ;; any address is valid
  (match (#'sut/admin-network? ["10.0.0.0/8"] "10.45.32.2") true)
  (match (#'sut/admin-network? ["10.0.0.0/8"] "11.45.32.2") false)
  (match (#'sut/admin-network? ["192.168.3.5"] "192.168.3.5") true)
  (match (#'sut/admin-network? ["192.168.0.0/16"] "192.168.2.3") true))

(deftest admin-network-check-test
  (let [success-result {:status 200 :body "hello"}
        cases          [{:request        {:remote-addr "127.0.0.1" :uri "/"}
                         :admin-networks ["127.0.0.0/8" ""]
                         :result         success-result}
                        {:request        {:remote-addr "128.0.0.1" :uri "/"}
                         :admin-networks ["127.0.0.0/8" ""]
                         :result         {:type :unifier.response/forbidden}}]

        handler        (fn [req] success-result)]
    (doseq [c cases]
      (let [mw (sut/admin-network-check handler (:admin-networks c))]
        (println {:request (:request c) :admin-networks (:admin-networks c)} "=>" (:result c))
        (match (mw (:request c)) (:result c))))))
