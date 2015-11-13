(ns groupify.core-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [groupify.core :refer :all]
            [groupify.ws-actions :refer :all]
            [groupify.util :as util]))

;(deftest test-app
;  (testing "main route"
;    (let [response (app (mock/request :get "/"))]
;      (is (= (:status response) 200))
;      (is (= (:body response) "Hello World"))))
;
;  (testing "not-found route"
;    (let [response (app (mock/request :get "/invalid"))]
;      (is (= (:status response) 404)))))

(deftest test-util
  (testing "atom append"
    (let [foo (atom [])]
      (is (= @foo []))
      (util/atom-append foo 1)
      (util/atom-append foo 2)
      (util/atom-append foo 3)
      (util/atom-append foo 4)
      (util/atom-append foo 5)
      (util/atom-append foo 6)
      (is (= @foo [1 2 3 4 5 6])))))

(deftest test-handle-default
  (testing "Testing unknown action"
    (let [data {}
          action "unknown"]
      (is (= (handle-default action data)
             "\"unknown\" is not a valid action for identity.")))))

(deftest test-handle-get-users
  (testing "Testing empty user list"
    (let [data {}
          action "unknown"]
      (is (= (handle-default action data)
             "\"unknown\" is not a valid action for identity.")))))

