(ns my.example.ctxdemo.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcho.core :refer [match]]
            [my.example.ctxdemo.core :as sut]))


(deftest ^:unit a-test
  (testing "simple test."
    (is (= 1 1))))
